package com.study.focus.announcement.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/announcements")
public class AnnouncementController {

    // 공지 목록 가져오기
    @GetMapping
    public void getAnnouncements(@PathVariable Long studyId) {}

    // 공지 생성하기
    @PostMapping
    public void createAnnouncement(@PathVariable Long studyId) {}

    // 공지 상세 데이터 가져오기
    @GetMapping("/{announcementId}")
    public void getAnnouncementDetail(@PathVariable Long studyId, @PathVariable Long announcementId) {}

    // 공지 수정하기
    @PutMapping("/{announcementId}")
    public void updateAnnouncement(@PathVariable Long studyId, @PathVariable Long announcementId) {}

    // 공지 삭제하기
    @DeleteMapping("/{announcementId}")
    public void deleteAnnouncement(@PathVariable Long studyId, @PathVariable Long announcementId) {}

    // 공지 상세 화면 댓글 작성
    @PostMapping("/{announcementId}/comments")
    public void addComment(@PathVariable Long studyId, @PathVariable Long announcementId) {}
}
