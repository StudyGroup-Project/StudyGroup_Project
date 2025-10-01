package com.study.focus.application.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.dto.GetApplicationDetailResponse;
import com.study.focus.application.dto.GetApplicationsResponse;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.RecruitStatus;
import com.study.focus.study.domain.StudyMember;
import com.study.focus.study.domain.StudyRole;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

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

    // 지원서 제출하기
    public Long submitApplication(Long applicantId, Long studyId, SubmitApplicationRequest request) {

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

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

        return applications.stream().map(app -> {
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
                    app.getCreatedAt(),
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
    public void handleApplication(Long studyId, Long applicationId) {
        // TODO: 지원서 상태 변경
    }
}
