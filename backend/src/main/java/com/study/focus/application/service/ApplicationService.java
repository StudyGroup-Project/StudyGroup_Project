package com.study.focus.application.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.dto.*;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.notification.service.NotificationService;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final UserProfileRepository userProfileRepository;
    private final S3Uploader s3Uploader;
    private final StudyMemberRepository studyMemberRepository;
    private final NotificationService notificationService;

    // 지원서 제출하기
    public Long submitApplication(Long applicantId, Long studyId, SubmitApplicationRequest request) {

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRoleAndStatus(studyId, StudyRole.LEADER, StudyMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // 스터디가 모집중인지 상태확인
        if (study.getRecruitStatus() != RecruitStatus.OPEN) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        // 지원서 reject당했을 때 다시 지원해도 예외처리 안해서 다시 지원할 수 있게
        applicationRepository.findByApplicantAndStudy(applicant, study)
                .ifPresent(application -> {
                    if (application.getStatus() != ApplicationStatus.REJECTED) {
                        throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
                    }
                });


        Application application = Application.builder()
                .applicant(applicant)
                .study(study)
                .content(request.getContent())
                .build();

        Application saveApplication = applicationRepository.save(application);
        //그룹에 새로운 지원서 알림 생성
        notificationService.addNewApplicationNotification(study,leaderMember.getUser().getId());
        return saveApplication.getId();
    }

    // 지원서 목록 가져오기
    @Transactional(readOnly = true)
    public List<GetApplicationsResponse> getApplications(Long studyId, Long requestUserId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 그룹장(방장) id 조회 및 권한 체크
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        User leader = leaderMember.getUser();
        if (!leader.getId().equals(requestUserId)) {
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN); // 방장만 접근 가능
        }

        List<Application> applications = applicationRepository.findByStudyId(studyId);

        return applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.SUBMITTED)
                .map(app -> {
                    Long applicantId = app.getApplicant().getId();
                    UserProfile userProfile = userProfileRepository.findByUserId(applicantId)
                            .orElseThrow(() -> new BusinessException(UserErrorCode.PROFILE_NOT_FOUND));
                    String nickname = userProfile.getNickname();
                    String profileImageUrl = userProfile.getProfileImage() != null
                            ? s3Uploader.getUrlFile(userProfile.getProfileImage().getFileKey())
                            : null;
                    return new GetApplicationsResponse(
                            app.getId(),
                            applicantId,
                            nickname,
                            profileImageUrl,
                            app.getCreatedAt(), // DTO 필드명(createAt)과 실제 필드명(getCreatedAt)이 일치하는지 확인하세요!
                            app.getStatus()
                    );
                }).toList();

    }

    // 지원서 상세 가져오기
    @Transactional(readOnly = true)
    public GetApplicationDetailResponse getApplicationDetail(Long studyId, Long applicationId, Long requestUserId) {
        // 방장인지 확인
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST)); // 스터디 리더 정보확인 에러
        if(!leaderMember.getUser().getId().equals(requestUserId)) {
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN); // 리더만 지원서 조회가능
        }

        // 지원서 조회
        Application application = applicationRepository.findByIdAndStudyId(applicationId, studyId)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_REQUEST));

        return new GetApplicationDetailResponse(application.getContent());
    }

    // 지원서 처리하기 (승인/거절 등)
    @Transactional
    public void handleApplication(Long studyId, Long applicationId, Long requestUserId, HandleApplicationRequest request) {

        // 방잠 권한 확인
        StudyMember leadermember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        if(!leadermember.getUser().getId().equals(requestUserId)){
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }

        // 지원서 조회
        Application application = applicationRepository.findByIdAndStudyId(applicationId, studyId)
                .orElseThrow(()->new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // 이미 처리된 지원서는 수정 불가
        if(application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        application.updateStatus(request.getStatus());

        // 지원서 수락으로 상태가 변경되면 스터디 멤버로 추가
        if(request.getStatus() == ApplicationStatus.ACCEPTED) {
            Study study = application.getStudy();

            int currentMemberCount = (int) studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED);
            if(currentMemberCount >= study.getMaxMemberCount()){
                throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
            }

            StudyMember newMember = StudyMember.builder()
                    .study(study)
                    .user(application.getApplicant())
                    .role(StudyRole.MEMBER)
                    .status(StudyMemberStatus.JOINED)
                    .build();
            studyMemberRepository.save(newMember);
            //그룹에 신규 회원 알림 생성
            notificationService.addNewMemberNotification(study, newMember.getUser().getId());
        }
    }
}
