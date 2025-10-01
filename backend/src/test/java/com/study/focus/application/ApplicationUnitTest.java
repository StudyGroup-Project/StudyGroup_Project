package com.study.focus.application;


import com.study.focus.account.domain.UserProfile;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.application.domain.Application;
import com.study.focus.application.dto.GetApplicationDetailResponse;
import com.study.focus.application.dto.GetApplicationsResponse;
import com.study.focus.application.dto.HandleApplicationRequest;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.application.service.ApplicationService;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.domain.File;
import com.study.focus.common.util.S3Uploader;
import com.study.focus.study.domain.*;
import com.study.focus.study.repository.StudyMemberRepository;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.application.domain.ApplicationStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationUnitTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private S3Uploader s3Uploader;
    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    @DisplayName("지원서 제출 - 성공")
    void submitApplication_Success() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 10L;
        final Long expectedApplicationId = 100L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("열심히 참여하겠습니다!");

        User fakeUser = User.builder().id(applicantId).build();
        Study fakeStudy = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        Application fakeApplication = Application.builder()
                .id(expectedApplicationId)
                .applicant(fakeUser)
                .study(fakeStudy)
                .content(request.getContent())
                .status(ApplicationStatus.SUBMITTED)
                .build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        given(applicationRepository.findByApplicantAndStudy(fakeUser, fakeStudy)).willReturn(Optional.empty());
        given(applicationRepository.save(any(Application.class))).willReturn(fakeApplication);

        // when
        Long actualApplicationId = applicationService.submitApplication(applicantId, studyId, request);

        // then
        assertThat(actualApplicationId).isEqualTo(expectedApplicationId);
        then(applicationRepository).should().save(any(Application.class));
    }

    @Test
    @DisplayName("지원서 제출 실패 - 모집중이 아닌 스터디")
    void submitApplication_Fail_StudyNotRecruiting() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 10L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");

        User fakeUser = User.builder().id(applicantId).build();
        Study fakeStudy = Study.builder().id(studyId).recruitStatus(RecruitStatus.CLOSED).build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));

        // when & then
        assertThatThrownBy(() -> applicationService.submitApplication(applicantId, studyId, request))
                .isInstanceOf(BusinessException.class);
        then(applicationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지원서 제출 실패 - 이미 지원서가 SUBMITTED 상태")
    void submitApplication_Fail_AlreadySubmitted() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 10L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");

        User fakeUser = User.builder().id(applicantId).build();
        Study fakeStudy = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        Application submittedApplication = Application.builder()
                .id(101L)
                .applicant(fakeUser)
                .study(fakeStudy)
                .content("이전 지원서")
                .status(ApplicationStatus.SUBMITTED)
                .build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        given(applicationRepository.findByApplicantAndStudy(fakeUser, fakeStudy)).willReturn(Optional.of(submittedApplication));

        // when & then
        assertThatThrownBy(() -> applicationService.submitApplication(applicantId, studyId, request))
                .isInstanceOf(BusinessException.class);
        then(applicationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지원서 제출 실패 - 이미 지원서가 ACCEPTED 상태")
    void submitApplication_Fail_AlreadyAccepted() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 10L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");

        User fakeUser = User.builder().id(applicantId).build();
        Study fakeStudy = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        Application acceptedApplication = Application.builder()
                .id(102L)
                .applicant(fakeUser)
                .study(fakeStudy)
                .content("이전 지원서")
                .status(ApplicationStatus.ACCEPTED)
                .build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        given(applicationRepository.findByApplicantAndStudy(fakeUser, fakeStudy)).willReturn(Optional.of(acceptedApplication));

        // when & then
        assertThatThrownBy(() -> applicationService.submitApplication(applicantId, studyId, request))
                .isInstanceOf(BusinessException.class);
        then(applicationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지원서 제출 성공 - 이전 지원서가 REJECTED 상태면 재지원 가능")
    void submitApplication_Success_ResubmitAfterRejected() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 10L;
        final Long expectedApplicationId = 200L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("재지원합니다!");

        User fakeUser = User.builder().id(applicantId).build();
        Study fakeStudy = Study.builder().id(studyId).recruitStatus(RecruitStatus.OPEN).build();
        Application rejectedApplication = Application.builder()
                .id(103L)
                .applicant(fakeUser)
                .study(fakeStudy)
                .content("이전 지원서")
                .status(ApplicationStatus.REJECTED)
                .build();
        Application newApplication = Application.builder()
                .id(expectedApplicationId)
                .applicant(fakeUser)
                .study(fakeStudy)
                .content(request.getContent())
                .status(ApplicationStatus.SUBMITTED)
                .build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(fakeStudy));
        given(applicationRepository.findByApplicantAndStudy(fakeUser, fakeStudy)).willReturn(Optional.of(rejectedApplication));
        given(applicationRepository.save(any(Application.class))).willReturn(newApplication);

        // when
        Long actualApplicationId = applicationService.submitApplication(applicantId, studyId, request);

        // then
        assertThat(actualApplicationId).isEqualTo(expectedApplicationId);
        then(applicationRepository).should().save(any(Application.class));
    }

    @Test
    @DisplayName("지원서 제출 실패 - 존재하지 않는 유저")
    void submitApplication_Fail_UserNotFound() {
        // given
        final Long applicantId = 999L;
        final Long studyId = 10L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");

        given(userRepository.findById(applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicationService.submitApplication(applicantId, studyId, request))
                .isInstanceOf(BusinessException.class);
        then(applicationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지원서 제출 실패 - 존재하지 않는 스터디")
    void submitApplication_Fail_StudyNotFound() {
        // given
        final Long applicantId = 1L;
        final Long studyId = 999L;
        SubmitApplicationRequest request = new SubmitApplicationRequest("지원합니다");
        User fakeUser = User.builder().id(applicantId).build();

        given(userRepository.findById(applicantId)).willReturn(Optional.of(fakeUser));
        given(studyRepository.findById(studyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicationService.submitApplication(applicantId, studyId, request))
                .isInstanceOf(BusinessException.class);
        then(applicationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지원서 목록 조회 - 방장 성공")
    void getApplications_Success_Leader() {
        // given
        Long studyId = 10L;
        Long leaderId = 1L;
        Long applicantId = 2L;
        ApplicationStatus status = ApplicationStatus.SUBMITTED;

        User leader = User.builder().id(leaderId).build();
        User applicant = User.builder().id(applicantId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();
        Application application = Application.builder()
                .id(100L)
                .applicant(applicant)
                .study(study)
                .status(status)
                .content("지원 내용")
                .build();

        // File 객체를 Mockito로 모킹
        File file = mock(File.class);
        String fileKey = "fileKey";
        String expectedUrl = "https://s3.com/fileKey";
        org.mockito.Mockito.when(file.getFileKey()).thenReturn(fileKey);

        UserProfile applicantProfile = UserProfile.builder()
                .user(applicant)
                .nickname("지원자")
                .profileImage(file)
                .build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByStudyId(studyId)).willReturn(List.of(application));
        given(userProfileRepository.findByUserId(applicantId)).willReturn(Optional.of(applicantProfile));
        given(s3Uploader.getUrlFile(fileKey)).willReturn(expectedUrl);
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));

        // when
        List<GetApplicationsResponse> result = applicationService.getApplications(studyId, leaderId);

        // then
        assertThat(result).hasSize(1);
        GetApplicationsResponse response = result.get(0);
        assertThat(response.getApplicantId()).isEqualTo(applicantId);
        assertThat(response.getNickname()).isEqualTo("지원자");
        assertThat(response.getProfileImageUrl()).isEqualTo(expectedUrl);
        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("지원서 목록 조회 실패 - 방장이 아닌 경우")
    void getApplications_Fail_NotLeader() {
        // given
        Long studyId = 10L;
        Long leaderId = 1L;
        Long notLeaderId = 99L;
        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then
        assertThatThrownBy(() -> applicationService.getApplications(studyId, notLeaderId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("지원서 목록 조회 실패 - 지원자 프로필 없음")
    void getApplications_Fail_ProfileNotFound() {
        // given
        Long studyId = 10L;
        Long leaderId = 1L;
        Long applicantId = 2L;
        User leader = User.builder().id(leaderId).build();
        User applicant = User.builder().id(applicantId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();
        Application application = Application.builder()
                .id(100L)
                .applicant(applicant)
                .study(study)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByStudyId(studyId)).willReturn(List.of(application));
        given(userProfileRepository.findByUserId(applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicationService.getApplications(studyId, leaderId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("지원서 상세 조회 - 방장 성공")
    void getApplicationDetail_Success_Leader() {
        // given
        Long studyId = 10L;
        Long applicationId = 100L;
        Long leaderId = 1L;
        String content = "안녕하세요! 저는 이 스터디에 꼭 참여하고 싶습니다. 열심히 하겠습니다!";
        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();
        Application application = Application.builder()
                .id(applicationId)
                .study(study)
                .content(content)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.of(application));

        // when
        GetApplicationDetailResponse response = applicationService.getApplicationDetail(studyId, applicationId, leaderId);

        // then
        assertThat(response.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("지원서 상세 조회 실패 - 방장이 아닌 경우")
    void getApplicationDetail_Fail_NotLeader() {
        // given
        Long studyId = 10L;
        Long applicationId = 100L;
        Long notLeaderId = 99L;
        User leader = User.builder().id(1L).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        // 방장 권한 체크에서 예외가 발생하므로, 지원서 조회 로직은 호출되지 않습니다.
        // 따라서 applicationRepository에 대한 stubbing은 필요 없습니다.

        // when & then
        assertThatThrownBy(() -> applicationService.getApplicationDetail(studyId, applicationId, notLeaderId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("지원서 상세 조회 실패 - 지원서 없음")
    void getApplicationDetail_Fail_ApplicationNotFound() {
        // given
        Long studyId = 10L;
        Long applicationId = 100L;
        Long leaderId = 1L;
        User leader = User.builder().id(leaderId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicationService.getApplicationDetail(studyId, applicationId, leaderId))
                .isInstanceOf(BusinessException.class);
    }


    @Test
    @DisplayName("지원서 수락 - 성공")
    void handleApplication_Accept_Success() {
        // given: 정원이 넉넉한 스터디의 지원서를 방장이 수락하는 상황
        final Long studyId = 1L;
        final Long applicationId = 10L;
        final Long leaderUserId = 100L;
        final HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        User leader = User.builder().id(leaderUserId).build();
        User applicant = User.builder().id(200L).build();
        Study mockStudy = mock(Study.class);
        Application mockApplication = mock(Application.class);
        StudyMember leaderMember = StudyMember.builder().user(leader).study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.of(mockApplication));
        given(mockApplication.getStatus()).willReturn(ApplicationStatus.SUBMITTED); // 초기 상태는 SUBMITTED
        given(mockApplication.getStudy()).willReturn(mockStudy);
        given(mockApplication.getApplicant()).willReturn(applicant);
        given(mockStudy.getMaxMemberCount()).willReturn(10);
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(5L);

        // when: 서비스 메서드 호출
        assertDoesNotThrow(() -> applicationService.handleApplication(studyId, applicationId, leaderUserId, request));

        // then: 지원서 상태가 'ACCEPTED'로 변경되고, 새로운 멤버가 저장되었는지 검증
        then(mockApplication).should().updateStatus(ApplicationStatus.ACCEPTED);

        ArgumentCaptor<StudyMember> memberCaptor = ArgumentCaptor.forClass(StudyMember.class);
        then(studyMemberRepository).should().save(memberCaptor.capture());

        StudyMember newMember = memberCaptor.getValue();
        assertThat(newMember.getUser()).isEqualTo(applicant);
        assertThat(newMember.getRole()).isEqualTo(StudyRole.MEMBER);
        assertThat(newMember.getStatus()).isEqualTo(StudyMemberStatus.JOINED);
    }

    @Test
    @DisplayName("지원서 거절 - 성공")
    void handleApplication_Reject_Success() {
        // given: 방장이 지원서를 거절하는 상황
        final Long studyId = 1L;
        final Long applicationId = 10L;
        final Long leaderUserId = 100L;
        final HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.REJECTED);

        User leader = User.builder().id(leaderUserId).build();
        Study mockStudy = mock(Study.class);
        Application mockApplication = mock(Application.class);
        StudyMember leaderMember = StudyMember.builder().user(leader).study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.of(mockApplication));
        given(mockApplication.getStatus()).willReturn(ApplicationStatus.SUBMITTED);

        // when: 서비스 메서드 호출
        assertDoesNotThrow(() -> applicationService.handleApplication(studyId, applicationId, leaderUserId, request));

        // then: 지원서 상태가 'REJECTED'로 변경되고, 새로운 멤버는 저장되지 않았는지 검증
        then(mockApplication).should().updateStatus(ApplicationStatus.REJECTED);
        then(studyMemberRepository).should(never()).save(any(StudyMember.class));
    }

    @Test
    @DisplayName("지원서 처리 실패 - 방장이 아닌 경우")
    void handleApplication_Fail_NotLeader() {
        // given: 방장이 아닌 사용자가 요청
        final Long studyId = 1L;
        final Long applicationId = 10L;
        final Long notLeaderUserId = 999L;
        final HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        User leader = User.builder().id(100L).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));

        // when & then: 권한 없음 예외 발생 검증
        assertThatThrownBy(() -> applicationService.handleApplication(studyId, applicationId, notLeaderUserId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("지원서 처리 실패 - 이미 처리된 지원서인 경우")
    void handleApplication_Fail_AlreadyProcessed() {
        // given: 이미 'ACCEPTED' 상태인 지원서
        final Long studyId = 1L;
        final Long applicationId = 10L;
        final Long leaderUserId = 100L;
        final HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.REJECTED);

        User leader = User.builder().id(leaderUserId).build();
        Study study = Study.builder().id(studyId).build();
        StudyMember leaderMember = StudyMember.builder().user(leader).study(study).role(StudyRole.LEADER).build();
        Application application = Application.builder().status(ApplicationStatus.ACCEPTED).build(); // 이미 처리됨

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.of(application));

        // when & then: 잘못된 요청 예외 발생 검증
        assertThatThrownBy(() -> applicationService.handleApplication(studyId, applicationId, leaderUserId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("지원서 수락 실패 - 정원이 가득 찬 경우")
    void handleApplication_Accept_Fail_StudyIsFull() {
        // given
        final Long studyId = 1L;
        final Long applicationId = 10L;
        final Long leaderUserId = 100L;
        final HandleApplicationRequest request = new HandleApplicationRequest(ApplicationStatus.ACCEPTED);

        User leader = User.builder().id(leaderUserId).build();
        Study mockStudy = mock(Study.class);
        Application mockApplication = mock(Application.class);
        StudyMember leaderMember = StudyMember.builder().user(leader).study(mockStudy).role(StudyRole.LEADER).build();

        given(studyMemberRepository.findByStudyIdAndRole(studyId, StudyRole.LEADER)).willReturn(Optional.of(leaderMember));
        given(applicationRepository.findByIdAndStudyId(applicationId, studyId)).willReturn(Optional.of(mockApplication));
        given(mockApplication.getStatus()).willReturn(ApplicationStatus.SUBMITTED);
        given(mockApplication.getStudy()).willReturn(mockStudy);
        given(mockStudy.getMaxMemberCount()).willReturn(5); // 최대 인원 5명
        given(studyMemberRepository.countByStudyIdAndStatus(studyId, StudyMemberStatus.JOINED)).willReturn(5L); // 현재 인원 5명

        // when & then
        assertThatThrownBy(() -> applicationService.handleApplication(studyId, applicationId, leaderUserId, request))
                .isInstanceOf(BusinessException.class);

        then(studyMemberRepository).should(never()).save(any(StudyMember.class));
    }

}
