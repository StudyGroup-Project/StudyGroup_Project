package com.study.focus.study.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.study.dto.GetStudiesResponse;
import com.study.focus.study.dto.SearchStudiesRequest;
import com.study.focus.study.dto.SearchStudiesResponse;
import com.study.focus.study.service.StudyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyQueryController {

    private final StudyQueryService studyQueryService;

    // 스터디 그룹 검색 요청
    @GetMapping
    public SearchStudiesResponse searchStudies(@ModelAttribute SearchStudiesRequest request,
                                               @AuthenticationPrincipal CustomUserDetails user) {
        return studyQueryService.searchStudies(request, user.getUserId());
    }

    // 내 그룹 데이터 가져오기
    @GetMapping("/mine")
    public ResponseEntity<GetStudiesResponse> getMyStudies(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(studyQueryService.getMyStudies(user.getUserId()));
    }

    // 내 찜 목록 가져오기
    @GetMapping("/bookmarks")
    public ResponseEntity<GetStudiesResponse> getBookmarks(@AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(studyQueryService.getBookmarks(user.getUserId()));
    }
}
