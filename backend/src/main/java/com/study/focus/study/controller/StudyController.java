package com.study.focus.study.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {
    private final StudyService studyService;

    // 그룹 생성
    @PostMapping
    public ResponseEntity<Void> createStudy(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CreateStudyRequest request
    ) {
        Long userId = user.getUserId();
        Long studyId = studyService.createStudy(userId, request);

        URI location = URI.create("/api/studies/" + studyId);

        return ResponseEntity.created(location).build();
    }

    // 그룹 프로필 정보 가져오기
    @GetMapping("/{studyId}")
    public void getStudyProfile(@PathVariable Long studyId) {}

    // 그룹 프로필 정보 수정하기
    @PutMapping("/{studyId}")
    public void updateStudyProfile(@PathVariable Long studyId) {}

    // 그룹 삭제
    @DeleteMapping("/{studyId}")
    public void deleteStudy(@PathVariable Long studyId) {}
}