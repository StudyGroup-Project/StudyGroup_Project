package com.study.focus.application.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/applications")
public class ApplicationController {

    // 지원서 제출하기
    @PostMapping
    public void submitApplication(@PathVariable Long studyId) {}

    // 지원서 목록 가져오기
    @GetMapping
    public void getApplications(@PathVariable Long studyId) {}

    // 지원서 상세 가져오기
    @GetMapping("/{applicationId}")
    public void getApplicationDetail(@PathVariable Long studyId, @PathVariable Long applicationId) {}

    // 지원서 처리하기
    @PutMapping("/{applicationId}")
    public void handleApplication(@PathVariable Long studyId, @PathVariable Long applicationId) {}
}
