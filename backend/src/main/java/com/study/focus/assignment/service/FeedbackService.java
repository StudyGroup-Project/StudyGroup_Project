package com.study.focus.assignment.service;

import com.study.focus.account.service.UserService;
import com.study.focus.assignment.domain.Feedback;
import com.study.focus.assignment.domain.Submission;
import com.study.focus.assignment.dto.EvaluateSubmissionRequest;
import com.study.focus.assignment.dto.GetFeedbackListResponse;
import com.study.focus.assignment.repository.AssignmentRepository;
import com.study.focus.assignment.repository.FeedbackRepository;
import com.study.focus.assignment.repository.SubmissionRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.service.GroupService;
import com.study.focus.study.domain.StudyMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final GroupService groupService;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    // 과제 평가하기
    @Transactional
    public Long addFeedback(Long studyId, Long assignmentId, Long submissionId, Long userId, EvaluateSubmissionRequest dto) {
        StudyMember reviewer = groupService.memberValidation(studyId,userId);
        assignmentRepository.findByIdAndStudyId(assignmentId, studyId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        Submission submission = submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));

        if(submission.getSubmitter().getId().equals(reviewer.getId())) throw new BusinessException(CommonErrorCode.INVALID_REQUEST);
        if(feedbackRepository.existsBySubmissionIdAndReviewerId(submissionId, reviewer.getId())) throw new BusinessException(CommonErrorCode.INVALID_REQUEST);

        Feedback feedback = Feedback.builder().submission(submission).reviewer(reviewer).content(dto.getContent()).score(dto.getScore()).build();
        submission.getSubmitter().getUser().updateTrustScore(dto.getScore());
        feedbackRepository.save(feedback);

        return feedback.getId();
    }

    // 과제 평가 목록 가져오기
    @Transactional(readOnly = true)
    public List<GetFeedbackListResponse> getFeedbacks(Long studyId, Long assignmentId, Long submissionId, Long userId) {
        groupService.memberValidation(studyId,userId);
        assignmentRepository.findByIdAndStudyId(assignmentId, studyId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        submissionRepository.findByIdAndAssignmentId(submissionId, assignmentId).orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_REQUEST));
        List<Feedback> feedbacks = feedbackRepository.findAllBySubmissionIdOrderByCreatedAtDescIdDesc(submissionId);

        return feedbacks.stream().map(a -> new GetFeedbackListResponse(a.getId(), a.getScore(),a.getContent(),a.getCreatedAt(),
                userService.getMyProfile(a.getReviewer().getUser().getId()).getNickname(),
                userService.getMyProfile(a.getReviewer().getUser().getId()).getProfileImageUrl())).toList();
    }
}

