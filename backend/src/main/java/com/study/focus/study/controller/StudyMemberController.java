package com.study.focus.study.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/members")
public class StudyMemberController {

    // 멤버 목록 가져오기
    @GetMapping
    public void getMembers(@PathVariable Long studyId) {}

    // 그룹 탈퇴
    @DeleteMapping("/me")
    public void leaveStudy(@PathVariable Long studyId) {}

    // 그룹 인원 추방하기 (방장)
    @DeleteMapping("/{userId}")
    public void expelMember(@PathVariable Long studyId, @PathVariable Long userId) {}
}