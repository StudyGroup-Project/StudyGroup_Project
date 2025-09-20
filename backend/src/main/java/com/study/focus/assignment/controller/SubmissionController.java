package com.study.focus.assignment.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments/{assignmentId}/submissions")
public class SubmissionController {

    // 과제 제출하기
    @PostMapping
    public void submitAssignment(@PathVariable Long studyId, @PathVariable Long assignmentId) {}

    // 과제 제출물 상세 데이터 가져오기
    @GetMapping("/{submissionId}")
    public void getSubmissionDetail(@PathVariable Long studyId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {}
}