package com.study.focus.announcement.controller;


import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.announcement.dto.AnnouncementUpdateDto;
import com.study.focus.announcement.dto.CreateCommentRequest;
import com.study.focus.announcement.dto.GetAnnouncementDetailResponse;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.announcement.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        log.info("Fetch announcements for studyId: {}", studyId);
        Long userId = user.getUserId();
        return ResponseEntity.ok(announcementService.findAllSummaries(studyId,userId));
    }

    // 공지 생성하기(multiPartFrom 형식만 허용) 방장만 허용
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createAnnouncement
    (@PathVariable Long studyId , @AuthenticationPrincipal CustomUserDetails user,
     @RequestParam(name = "title") String title, @RequestParam(name = "content") String content,
     @RequestPart(name = "files",required = false) List<MultipartFile> files)
    {
        log.info("Create announcement for studyId: {}", studyId);
        Long userId = user.getUserId();
        Long savedAnnouncementId = announcementService.createAnnouncement(studyId, userId, title, content, files);
        return new ResponseEntity<>( HttpStatus.CREATED);
    }

    // 공지 수정하기
    @PutMapping(path ="/{announcementId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateAnnouncement(@PathVariable Long studyId, @PathVariable Long announcementId
    , @AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute AnnouncementUpdateDto announcement)
    {
        log.info("Update announcement for studyId: {} , for announcementId: {}", studyId,announcementId);
        Long userId = user.getUserId();
        announcementService.updateAnnouncement(studyId,announcementId,userId, announcement);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 공지 삭제하기
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long studyId, @PathVariable Long announcementId ,
                                   @AuthenticationPrincipal CustomUserDetails user) {
        log.info("Delete announcement for studyId: {} , for announcementId: {}", studyId,announcementId);
        Long userId = user.getUserId();
        announcementService.deleteAnnouncement(studyId,userId,announcementId);
        return new ResponseEntity<>(HttpStatus.OK);
    }




    // 공지 상세 데이터 가져오기
    @GetMapping("/{announcementId}")
    public ResponseEntity<GetAnnouncementDetailResponse> getAnnouncementDetail(@PathVariable Long studyId,
                                                                               @PathVariable Long announcementId,
                                                                               @AuthenticationPrincipal CustomUserDetails user) {
        log.info("Get announcement Detail Data for studyId: {} , for announcementId: {}", studyId,announcementId);
        Long userId = user.getUserId();
        GetAnnouncementDetailResponse detailResponse = announcementService.getAnnouncementDetail(studyId, announcementId, userId);
        return ResponseEntity.ok(detailResponse);
    }



    // 공지 상세 화면 댓글 작성
    @PostMapping("/{announcementId}/comments")
    public ResponseEntity<Void> addComment(@PathVariable Long studyId, @PathVariable Long announcementId,
                           @RequestBody CreateCommentRequest commentRequest,
                           @AuthenticationPrincipal CustomUserDetails user) {
        log.info("add Comment at Announcement for studyId: {} , for announcementId: {}", studyId,announcementId);
        Long userId = user.getUserId();
        announcementService.addComment(studyId,announcementId,userId,commentRequest);
        return new ResponseEntity<>( HttpStatus.CREATED);
    }
}
