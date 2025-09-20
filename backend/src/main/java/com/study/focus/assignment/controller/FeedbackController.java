package com.study.focus.assignment.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments/{assignmentId}/submissions/{submissionId}/feedbacks")
public class FeedbackController {

    // 과제 평가하기
    @PostMapping
    public void addFeedback(@PathVariable Long studyId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {}

    // 과제 평가 목록 가져오기
    @GetMapping
    public void getFeedbacks(@PathVariable Long studyId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {}
}
