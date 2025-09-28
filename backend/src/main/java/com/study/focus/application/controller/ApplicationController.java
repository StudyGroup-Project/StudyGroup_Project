package com.study.focus.application.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.application.dto.SubmitApplicationRequest;
import com.study.focus.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies/{studyId}/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    // 지원서 제출하기
    @PostMapping
    public ResponseEntity<Void> submitApplication(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user,
                                                  @RequestBody SubmitApplicationRequest request) {

        Long userId = user.getUserId();

        Long applicationId = applicationService.submitApplication( userId, studyId, request);

        URI location = URI.create(String.format("/api/studies/%d/applications/%d", studyId, applicationId));

        return ResponseEntity.created(location).build();

    }

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
