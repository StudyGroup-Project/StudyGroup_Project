package com.study.focus.study.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
public class StudyQueryController {

    // 스터디 그룹 검색 요청
    @GetMapping
    public void searchStudies() {}

    // 내 그룹 데이터 가져오기
    @GetMapping("/mine")
    public void getMyStudies() {}

    // 내 찜 목록 가져오기
    @GetMapping("/bookmarks")
    public void getBookmarks() {}
}
