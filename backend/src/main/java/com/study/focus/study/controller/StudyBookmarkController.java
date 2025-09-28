package com.study.focus.study.controller;

import com.study.focus.study.service.BookmarkService;
import com.study.focus.account.dto.CustomUserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/studies/{studyId}/bookmark")
@RequiredArgsConstructor
public class StudyBookmarkController {
    private final BookmarkService bookmarkService;

    // 스터디 그룹 찜하기
    @PostMapping
    public ResponseEntity<Void> addBookmark(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user) {
        Long userId = user.getUserId();
        bookmarkService.addBookmark(userId, studyId);

        return ResponseEntity.ok().build();
    }

    // 스터디 그룹 찜 해제하기
    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(@PathVariable Long studyId,  @AuthenticationPrincipal CustomUserDetails user) {

        bookmarkService.removeBookmark(user.getUserId(), studyId);
        return ResponseEntity.noContent().build();
    }
}
