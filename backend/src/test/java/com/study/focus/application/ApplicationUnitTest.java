package com.study.focus.application;


import com.study.focus.application.domain.Application;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.repository.ApplicationRepository;
import com.study.focus.application.service.ApplicationService;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.RecruitStatus;
import com.study.focus.study.repository.StudyRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.application.domain.ApplicationStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ApplicationUnitTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudyRepository studyRepository;

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

}
