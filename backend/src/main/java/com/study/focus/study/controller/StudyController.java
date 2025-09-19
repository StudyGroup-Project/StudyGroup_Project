package com.study.focus.study.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
public class StudyController {

    // 그룹 생성
    @PostMapping
    public void createStudy() {}

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