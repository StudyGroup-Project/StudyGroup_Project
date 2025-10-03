package com.study.focus.study.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.study.dto.CreateStudyRequest;
import com.study.focus.study.dto.GetStudyProfileResponse;
import com.study.focus.study.dto.StudyHomeResponse;
import com.study.focus.study.dto.UpdateStudyProfileRequest;
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
    public ResponseEntity<GetStudyProfileResponse> getStudyProfile(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        GetStudyProfileResponse response = studyService.getStudyProfile(studyId, userId);
        return ResponseEntity.ok(response);
    }

    // 그룹 프로필 정보 수정하기
    @PutMapping("/{studyId}")
    public ResponseEntity<Void> updateStudyProfile(
            @PathVariable Long studyId,
            @RequestBody UpdateStudyProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long requestUserId = user.getUserId();
        studyService.updateStudyProfile(studyId, requestUserId, request);
        return ResponseEntity.ok().build();
    }

    //스터디 메인 데이터 조회하기
    @GetMapping("/{studyId}/home")
    public ResponseEntity<StudyHomeResponse> getStudyHome(@PathVariable Long studyId) {
        StudyHomeResponse response = studyService.getStudyHome(studyId);
        return ResponseEntity.ok(response);
    }

    // 그룹 삭제
    @DeleteMapping("/{studyId}")
    public ResponseEntity<Void> deleteStudy(@PathVariable Long studyId,
                                            @AuthenticationPrincipal CustomUserDetails user) {
        Long requestUserId = user.getUserId();
        studyService.deleteStudy(studyId, requestUserId);
        return ResponseEntity.noContent().build();
    }
}