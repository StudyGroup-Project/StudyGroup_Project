package com.study.focus.resource.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/resources")
@Slf4j
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    // 자료 목록 가져오기
    @GetMapping
    public ResponseEntity<List<GetResourcesResponse>> getResources(
            @PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user)
    {
        log.info("Get ResourceList for studyId: {}",studyId);
        Long userId = user.getUserId();
        List<GetResourcesResponse> result = resourceService.getResources(studyId, userId);
        return ResponseEntity.ok(result);
    }

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
