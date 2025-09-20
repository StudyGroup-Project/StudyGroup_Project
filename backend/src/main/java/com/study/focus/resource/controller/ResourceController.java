package com.study.focus.resource.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/resources")
public class ResourceController {

    // 자료 목록 가져오기
    @GetMapping
    public void getResources(@PathVariable Long studyId) {}

    // 자료 생성
    @PostMapping
    public void createResource(@PathVariable Long studyId) {}

    // 자료 상세 데이터 가져오기
    @GetMapping("/{resourceId}")
    public void getResourceDetail(@PathVariable Long studyId, @PathVariable Long resourceId) {}

    // 자료 수정
    @PutMapping("/{resourceId}")
    public void updateResource(@PathVariable Long studyId, @PathVariable Long resourceId) {}

    // 자료 삭제
    @DeleteMapping("/{resourceId}")
    public void deleteResource(@PathVariable Long studyId, @PathVariable Long resourceId) {}
}
