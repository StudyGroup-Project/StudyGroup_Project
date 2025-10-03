package com.study.focus.study.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.study.service.StudyMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/studies/{studyId}/members")
public class StudyMemberController {

    private final StudyMemberService studyMemberService;

    public StudyMemberController(StudyMemberService studyMemberService) {
        this.studyMemberService = studyMemberService;
    }

    // 멤버 목록 가져오기
    @GetMapping
    public void getMembers(@PathVariable Long studyId) {}

    // 그룹 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveStudy(@PathVariable Long studyId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long requestUserId = userDetails.getUserId();
        studyMemberService.leaveStudy(studyId, requestUserId);

        return ResponseEntity.noContent().build();
    }

    // 그룹 인원 추방하기 (방장)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> expelMember(@PathVariable Long studyId,
                                              @PathVariable("userId") Long userId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long requestUserId = userDetails.getUserId();
        studyMemberService.expelMember(studyId, userId, requestUserId);

        return ResponseEntity.noContent().build();
    }
}