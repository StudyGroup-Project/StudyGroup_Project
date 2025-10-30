package com.study.focus.assignment;

import com.study.focus.account.domain.User;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.service.UserService;
import com.study.focus.assignment.domain.Assignment;
import com.study.focus.assignment.domain.Feedback;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.EvaluateSubmissionRequest;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.FeedbackRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.assignment.service.FeedbackService;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.service.GroupService;
import com.study.focus.study.domain.Study;
import com.study.focus.study.domain.StudyMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackUnitTest {

    @InjectMocks
    private FeedbackService feedbackService;

    @Mock private GroupService groupService;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private UserService userService;

    private final Long studyId = 1L;
    private final Long assignmentId = 10L;
    private final Long submissionId = 777L;
    private final Long userId = 100L;

    private Study study;
    private Assignment assignment;
    private StudyMember reviewer;     // 피드백 작성자
    private StudyMember submitter;    // 제출물 작성자
    private Submission submission;

    @BeforeEach
    void setUp() {
        study = Study.builder().id(studyId).build();

        reviewer = StudyMember.builder()
                .id(200L)
                .study(study)
                .build();

        submitter = StudyMember.builder()
                .id(201L)
                .study(study)
                .build();

        assignment = Assignment.builder()
                .id(assignmentId)
                .study(study)
                .dueAt(LocalDateTime.now().plusHours(1))
                .build();

        submission = Submission.builder()
                .id(submissionId)
                .assignment(assignment)
                .submitter(submitter)
                .description("desc")
                .build();

        // 제출자 User 목과 trustScore 업데이트 감시
        var user = Mockito.mock(User.class);
        ReflectionTestUtils.setField(submitter, "user", user);
        var reviewerUser = Mockito.mock(User.class);
        ReflectionTestUtils.setField(reviewer, "user", reviewerUser);

    }

    private EvaluateSubmissionRequest realDto(Long score, String content) {
        EvaluateSubmissionRequest dto = EvaluateSubmissionRequest.builder().score(score).content(content).build();
        return dto;
    }

    private GetMyProfileResponse mockProfile(Long userId, String nickname, String imageUrl) {
        var dto = Mockito.mock(com.study.focus.account.dto.GetMyProfileResponse.class);
        given(dto.getNickname()).willReturn(nickname);
        given(dto.getProfileImageUrl()).willReturn(imageUrl);
        return dto;
    }

    private Feedback makeFeedback(Long id, Long score, String content, LocalDateTime createdAt, StudyMember reviewer, Submission submission) {
        Feedback f = Mockito.mock(Feedback.class, Answers.RETURNS_DEEP_STUBS);
        given(f.getId()).willReturn(id);
        given(f.getScore()).willReturn(score);
        given(f.getContent()).willReturn(content);
        given(f.getCreatedAt()).willReturn(createdAt);
        given(f.getReviewer()).willReturn(reviewer);
        given(f.getSubmission()).willReturn(submission);
        return f;
    }

    @Test
    @DisplayName("성공: 정상 피드백 작성")
    void addFeedback_success() {
        // given
        EvaluateSubmissionRequest dto = realDto(3L, "good");

        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(submission));
        given(feedbackRepository.existsBySubmissionIdAndReviewerId(submissionId, reviewer.getId())).willReturn(false);

        // save 시 ID 주입
        willAnswer(inv -> {
            Feedback f = inv.getArgument(0);
            ReflectionTestUtils.setField(f, "id", 1234L);
            return f;
        }).given(feedbackRepository).save(any(Feedback.class));

        // when
        Long id = feedbackService.addFeedback(studyId, assignmentId, submissionId, userId, dto);

        // then
        assertThat(id).isEqualTo(1234L);
        then(groupService).should(times(1)).memberValidation(studyId, userId);
        then(assignmentRepository).should(times(1)).findByIdAndStudyId(assignmentId, studyId);
        then(submissionRepository).should(times(1)).findByIdAndAssignmentId(submissionId, assignmentId);
        then(feedbackRepository).should(times(1)).existsBySubmissionIdAndReviewerId(submissionId, reviewer.getId());
        then(feedbackRepository).should(times(1)).save(any(Feedback.class));

        // 신뢰점수 갱신 호출 검증
        var submitterUser = (com.study.focus.account.domain.User) ReflectionTestUtils.getField(submitter, "user");
        then(submitterUser).should(times(1)).updateTrustScore(3L);
    }

    @Test
    @DisplayName("실패: studyId가 null")
    void addFeedback_fail_null_studyId() {
        // given
        EvaluateSubmissionRequest dto = realDto(1L, "x");
        given(groupService.memberValidation(null, userId))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() ->
                feedbackService.addFeedback(null, assignmentId, submissionId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패: userId가 null")
    void addFeedback_fail_null_userId() {
        // given
        EvaluateSubmissionRequest dto = realDto(1L, "x");
        given(groupService.memberValidation(studyId, null))
                .willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() ->
                feedbackService.addFeedback(studyId, assignmentId, submissionId, null, dto)
        ).isInstanceOf(BusinessException.class);

        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패: assignmentId가 null")
    void addFeedback_fail_null_assignmentId() {
        // given
        EvaluateSubmissionRequest dto = realDto(1L, "x");
        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(null, studyId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                feedbackService.addFeedback(studyId, null, submissionId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(assignmentRepository).should(times(1)).findByIdAndStudyId(null, studyId);
        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("실패: 제출자가 자기 제출물에 피드백 시도")
    void addFeedback_fail_self_review() {
        // given - reviewer == submitter (동일 StudyMember)
        EvaluateSubmissionRequest dto = realDto(2L, "self");
        // reviewer를 submitter로 바꿔 반환
        given(groupService.memberValidation(studyId, userId)).willReturn(submitter);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(submission));

        // when / then
        assertThatThrownBy(() ->
                feedbackService.addFeedback(studyId, assignmentId, submissionId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(feedbackRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 이미 피드백을 한 리뷰어의 중복 제출")
    void addFeedback_fail_duplicate_feedback() {
        // given
        EvaluateSubmissionRequest dto = realDto(1L, "dup");

        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(submission));
        given(feedbackRepository.existsBySubmissionIdAndReviewerId(submissionId, reviewer.getId())).willReturn(true);

        // when / then
        assertThatThrownBy(() ->
                feedbackService.addFeedback(studyId, assignmentId, submissionId, userId, dto)
        ).isInstanceOf(BusinessException.class);

        then(feedbackRepository).should(never()).save(any());
    }

    /* 과제 피드백 목록 조회 test */

    @DisplayName("성공: 피드백 목록 조회 - 아이템 존재")
    @Test
    void getFeedbacks_success_with_items() {
        // given
        // member 검증 OK
        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        // 과제/제출물 존재
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(submission));

        var reviewerUser = (User) ReflectionTestUtils.getField(reviewer, "user");
        given(reviewerUser.getId()).willReturn(900L);

        // 피드백 2건
        Feedback f1 = makeFeedback(1L, 5L, "great", LocalDateTime.now().minusMinutes(3), reviewer, submission);
        Feedback f2 = makeFeedback(2L, 3L, "good", LocalDateTime.now().minusMinutes(1), reviewer, submission);
        given(feedbackRepository.findAllBySubmissionId(submissionId)).willReturn(java.util.List.of(f1, f2));

        // 프로필 조회 목
        var profile = mockProfile(900L, "alice", "img://alice");
        // 서비스가 피드백마다 2회 호출(닉네임/이미지)하므로 단순 리턴
        given(userService.getMyProfile(900L)).willReturn(profile);

        // when
        var result = feedbackService.getFeedbacks(studyId, assignmentId, submissionId, userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(5L);
        assertThat(result.get(0).getEvaluatorName()).isEqualTo("alice");
        assertThat(result.get(0).getEvaluatorProfileUrl()).isEqualTo("img://alice");

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getScore()).isEqualTo(3L);

        then(groupService).should().memberValidation(studyId, userId);
        then(assignmentRepository).should().findByIdAndStudyId(assignmentId, studyId);
        then(submissionRepository).should().findByIdAndAssignmentId(submissionId, assignmentId);
        then(feedbackRepository).should().findAllBySubmissionId(submissionId);
        then(userService).should(atLeastOnce()).getMyProfile(900L);
    }

    @DisplayName("성공: 피드백 목록 조회 - 빈 리스트")
    @Test
    void getFeedbacks_success_empty_list() {
        // given
        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId)).willReturn(Optional.of(submission));
        given(feedbackRepository.findAllBySubmissionId(submissionId)).willReturn(java.util.List.of());

        // when
        var result = feedbackService.getFeedbacks(studyId, assignmentId, submissionId, userId);

        // then
        assertThat(result).isEmpty();
        then(userService).shouldHaveNoInteractions(); // 피드백 없으니 프로필 조회 없음
    }

    @DisplayName("실패: studyId가 null")
    @Test
    void getFeedbacks_fail_null_studyId() {
        // given
        given(groupService.memberValidation(null, userId)).willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() -> feedbackService.getFeedbacks(null, assignmentId, submissionId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("실패: assignmentId가 null")
    @Test
    void getFeedbacks_fail_null_assignmentId() {
        // given
        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(null, studyId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> feedbackService.getFeedbacks(studyId, null, submissionId, userId))
                .isInstanceOf(BusinessException.class);

        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("실패: submissionId가 null")
    @Test
    void getFeedbacks_fail_null_submissionId() {
        // given
        given(groupService.memberValidation(studyId, userId)).willReturn(reviewer);
        given(assignmentRepository.findByIdAndStudyId(assignmentId, studyId)).willReturn(Optional.of(assignment));
        given(submissionRepository.findByIdAndAssignmentId(null, assignmentId)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> feedbackService.getFeedbacks(studyId, assignmentId, null, userId))
                .isInstanceOf(BusinessException.class);

        then(feedbackRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }

    @DisplayName("실패: 해당 user가 study에 속하지 않음 (memberValidation 예외)")
    @Test
    void getFeedbacks_fail_user_not_member() {
        // given
        given(groupService.memberValidation(studyId, userId)).willThrow(new BusinessException(CommonErrorCode.INVALID_REQUEST));

        // when / then
        assertThatThrownBy(() -> feedbackService.getFeedbacks(studyId, assignmentId, submissionId, userId))
                .isInstanceOf(BusinessException.class);

        then(assignmentRepository).shouldHaveNoInteractions();
        then(submissionRepository).shouldHaveNoInteractions();
        then(feedbackRepository).shouldHaveNoInteractions();
        then(userService).shouldHaveNoInteractions();
    }
}
