package com.study.focus.study.service;

import com.study.focus.account.domain.User;
import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.announcement.domain.Announcement;
import com.study.focus.announcement.repository.AnnouncementRepository;
import com.study.focus.announcement.repository.CommentRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.domain.ApplicationStatus;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.FeedbackRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.chat.repository.ChatMessageRepository;
import com.study.focus.common.domain.Address;
import com.study.focus.common.dto.StudyDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import com.study.focus.common.repository.FileRepository;
import com.study.focus.common.service.FileService;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.notification.repository.NotificationRepository;
import com.study.focus.resource.domain.Resource;
import com.study.focus.resource.repository.ResourceRepository;
import com.study.focus.study.domain.*;
import com.study.focus.study.dto.*;
import com.study.focus.study.repository.BookmarkRepository;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.dto.GetStudyProfileResponse;
import com.study.focus.study.dto.StudyHomeResponse;
import com.study.focus.study.dto.UpdateStudyProfileRequest;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyProfileRepository;
import com.study.focus.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {
    private final StudyRepository studyRepository;
    private final StudyProfileRepository studyProfileRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final UserProfileRepository userProfileRepository;
    private final BookmarkRepository bookmarkRepository;
    private final S3Uploader s3Uploader;

    private final AnnouncementRepository announcementRepository;
    private final CommentRepository commentRepository;
    private final AssignmentRepository assignmentRepository;
    private final FeedbackRepository feedbackRepository;
    private final SubmissionRepository submissionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final ResourceRepository resourceRepository;
    private final FileService fileService;

    // 스터디 그룹 생성
    public Long createStudy(Long userId, CreateStudyRequest createStudyRequest ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.URL_FORBIDDEN));
        // 스터디 생성
        Study study = Study.builder()
                .maxMemberCount(createStudyRequest.getMaxMemberCount())
                .recruitStatus(RecruitStatus.OPEN)
                .build();
        Study saveStudy = studyRepository.save(study);

        //스터디 프로필 생성
        Address address = Address.builder()
                .province(createStudyRequest.getProvince())
                .district(createStudyRequest.getDistrict())
                .build();

        StudyProfile studyProfile = StudyProfile.builder()
                .study(saveStudy)
                .title(createStudyRequest.getTitle())
                .bio(createStudyRequest.getBio())
                .description(createStudyRequest.getDescription())
                .address(address)
                .category(createStudyRequest.getCategory())
                .build();
        studyProfileRepository.save(studyProfile);


        StudyMember leader = StudyMember.builder()
                .study(saveStudy)
                .user(user)
                .role(StudyRole.LEADER)
                .status(StudyMemberStatus.JOINED)
                .build();
        studyMemberRepository.save(leader);

        // 스터디의 ID 반환
        return saveStudy.getId();
    }

    // 그룹 프로필 정보 가져오기
    @Transactional(readOnly = true)
    public GetStudyProfileResponse getStudyProfile(Long studyId, Long userId) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.INACTIVE_USER));
        StudyProfile profile = studyProfileRepository.findByStudy(study)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 그룹장 id조회
        StudyMember leadermember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        User leader = leadermember.getUser();

        //그룹장 프로필 조회
        UserProfile leaderProfile = userProfileRepository.findByUser(leader)
                .orElseThrow(() -> new BusinessException(UserErrorCode.URL_FORBIDDEN));
        String nickname = leaderProfile.getNickname();
        String profileImageUrl = leaderProfile.getProfileImage() != null
                ? s3Uploader.getUrlFile(leaderProfile.getProfileImage().getFileKey())
                : null;

        // 현재 인원
        int memberCount = (int) studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED);

        // 지원서 조회 ,없으면 null
        Application application = applicationRepository.findByApplicantIdAndStudyId(userId, studyId).orElse(null);
        String applicationStatus = (application != null) ? application.getStatus().name() : null;

        // 추방정보확인
        boolean isBanned = studyMemberRepository.existsByStudyIdAndUserIdAndStatus(studyId, userId, StudyMemberStatus.BANNED);

        // 지원 가능 여부 ( 프론트에서 확인하기 위함.)
        boolean canApply = !isBanned && study.getRecruitStatus() == RecruitStatus.OPEN
                && (applicationStatus == null || "REJECTED".equals(applicationStatus));

        // 방장의 신뢰 점수
        int trustScore = (int) leader.getTrustScore();

        return new GetStudyProfileResponse(
                study.getId(),
                profile.getTitle(),
                profile.getStudy().getMaxMemberCount(),
                memberCount,
                profile.getBio(),
                profile.getDescription(),
                profile.getCategory(),
                profile.getAddress().getProvince(),
                profile.getAddress().getDistrict(),
                study.getRecruitStatus(),
                trustScore,
                applicationStatus,
                canApply,
                new GetStudyProfileResponse.LeaderProfile(
                        leader.getId(),
                        nickname,
                        profileImageUrl
                )
        );
    }

    // 그룹 프로필 정보 수정하기
    public void updateStudyProfile(Long studyId, Long requestUserId, UpdateStudyProfileRequest request) {

        //방장 권한
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        if(!leaderMember.getUser().getId().equals(requestUserId)){
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_PARAMETER));
        StudyProfile studyProfile = studyProfileRepository.findByStudy(study)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        // 최대 멤버는 현재 참여중인 멤버보다 적으면 안됨.
        int currentMemberCount = (int) studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED);
        if(request.getMaxMemberCount() < currentMemberCount){
            throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        }

        study.updateMaxMemberCount(request.getMaxMemberCount());

        studyProfile.update(
                request.getTitle(),
                request.getCategory(),
                new Address(request.getProvince(), request.getDistrict()),
                request.getBio(),
                request.getDescription()
        );
    }

    //스터디 메인 데이터 조회하기
    @Transactional(readOnly = true)
    public StudyHomeResponse getStudyHome(Long studyId) {

        StudyProfile studyProfile = studyProfileRepository.findByStudyId(studyId)
                .orElseThrow(()-> new BusinessException(CommonErrorCode.INVALID_PARAMETER));

        return new StudyHomeResponse(studyProfile.getTitle());
    }

    // 그룹 삭제
    @Transactional
    public void deleteStudy(Long studyId, Long requestUserId) {
        //방장 권한
        StudyMember leaderMember = studyMemberRepository.findByStudyIdAndRoleAndStatus(studyId, StudyRole.LEADER, StudyMemberStatus.JOINED)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));

        if(!leaderMember.getUser().getId().equals(requestUserId)){
            throw new BusinessException(UserErrorCode.URL_FORBIDDEN);
        }

        // 스터디에 속한 모든 자식 엔티티 조회
        List<Announcement> announcements = announcementRepository.findAllByStudyId(studyId);
        List<Assignment> assignments = assignmentRepository.findAllByStudyId(studyId);
        List<Resource> resources = resourceRepository.findAllByStudyId(studyId);

        // 각 엔티티의 파일들을 삭제하도록 fileservice에 위임.
        announcements.forEach(an -> fileService.deleteFilesByAnnouncementId(an.getId()));

        assignments.forEach(as -> {
            //과제 + 제출물 전부 삭제
            List<Long> submissionIds = submissionRepository.findIdsByAssignmentId(as.getId());
            fileService.deleteFilesBySubmissionIds(submissionIds);
            fileService.deleteFilesByAssignmentId(as.getId());
        });

        // 자료 삭제
        resources.forEach(r -> fileService.deleteFilesByResourceId(r.getId()));

        commentRepository.deleteAllByAnnouncement_Study_Id(studyId);
        feedbackRepository.deleteAllBySubmission_Assignment_Study_Id(studyId);
        submissionRepository.deleteAllByAssignment_Study_Id(studyId);

        announcementRepository.deleteAllByStudy_Id(studyId);
        assignmentRepository.deleteAllByStudy_Id(studyId);

        applicationRepository.deleteAllByStudy_Id(studyId);
        bookmarkRepository.deleteAllByStudy_Id(studyId);
        chatMessageRepository.deleteAllByStudy_Id(studyId);
        notificationRepository.deleteAllByStudy_Id(studyId);
        studyMemberRepository.deleteAllByStudy_Id(studyId);
        studyProfileRepository.deleteByStudy_Id(studyId);
        resourceRepository.deleteAllByStudy_Id(studyId);

        studyRepository.deleteById(studyId);

    }
}