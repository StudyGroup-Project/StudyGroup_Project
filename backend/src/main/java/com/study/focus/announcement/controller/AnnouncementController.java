package com.study.focus.announcement.controller;


import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/studies/{studyId}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    //스터디 공지 목록 가져오기
    @GetMapping
    public ResponseEntity<List<GetAnnouncementsResponse>> getAnnouncements
    (@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user)
    {
        log.info("Fetching announcements for studyId: {}", studyId);
        Long userId = user.getUserId();
        return ResponseEntity.ok(announcementService.findAllSummaries(studyId,userId));
    }

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
