package com.study.focus.assignment;

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
        var user = Mockito.mock(com.study.focus.account.domain.User.class);
        ReflectionTestUtils.setField(submitter, "user", user);
    }

    private EvaluateSubmissionRequest realDto(Long score, String content) {
        EvaluateSubmissionRequest dto = new EvaluateSubmissionRequest();
        ReflectionTestUtils.setField(dto, "score", score);
        ReflectionTestUtils.setField(dto, "content", content);
        return dto;
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
}
