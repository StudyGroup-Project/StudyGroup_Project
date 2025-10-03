package com.study.focus.assignment.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.assignment.dto.CreateSubmissionRequest;
import com.study.focus.assignment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/studies/{studyId}/assignments/{assignmentId}/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    // 과제 제출하기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> submitAssignment(@PathVariable Long studyId, @PathVariable Long assignmentId,
                                           @AuthenticationPrincipal CustomUserDetails user,
                                           @ModelAttribute CreateSubmissionRequest dto)
    {
        Long userId = user.getUserId();
        Long CreateSubmissionId = submissionService.submitSubmission(studyId,assignmentId,userId,dto);
        URI location = URI.create("/api/studies/" + studyId + "/assignments/" + assignmentId + "/submissions/" + CreateSubmissionId);
        return ResponseEntity.created(location).build();
    }

    // 과제 제출물 상세 데이터 가져오기
    @GetMapping("/{submissionId}")
    public void getSubmissionDetail(@PathVariable Long studyId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {}
}