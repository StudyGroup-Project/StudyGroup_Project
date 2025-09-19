package com.study.focus.study.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/bookmark")
public class StudyBookmarkController {

    // 스터디 그룹 찜하기
    @PostMapping
    public void addBookmark(@PathVariable Long studyId) {}

    // 스터디 그룹 찜 해제하기
    @DeleteMapping
    public void removeBookmark(@PathVariable Long studyId) {}
}
