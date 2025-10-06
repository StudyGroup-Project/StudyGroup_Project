package com.study.focus.assignment.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.assignment.dto.EvaluateSubmissionRequest;
import com.study.focus.assignment.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies/{studyId}/assignments/{assignmentId}/submissions/{submissionId}/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 과제 평가하기
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addFeedback(@PathVariable Long studyId, @PathVariable Long assignmentId,
                                            @PathVariable Long submissionId, @AuthenticationPrincipal CustomUserDetails user,
                                            @RequestBody @Valid EvaluateSubmissionRequest dto)
    {
        Long userId = user.getUserId();
        Long feedbackId = feedbackService.addFeedback(studyId, assignmentId, submissionId, userId, dto);
        URI location = URI.create("/api/studies/" + studyId + "/assignments/" + assignmentId + "/submissions/" + submissionId + "/feedbacks/" + feedbackId);
        return ResponseEntity.created(location).build();
    }

    // 과제 평가 목록 가져오기
    @GetMapping
    public void getFeedbacks(@PathVariable Long studyId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {}
}
