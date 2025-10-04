package com.study.focus.resource.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.announcement.dto.GetAnnouncementsResponse;
import com.study.focus.resource.dto.CreateResourceRequest;
import com.study.focus.resource.dto.GetResourceDetailResponse;
import com.study.focus.resource.dto.GetResourcesResponse;
import com.study.focus.resource.dto.UpdateResourceRequest;
import com.study.focus.resource.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createResource(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user
                               , @Valid @ModelAttribute CreateResourceRequest resourceRequest
                               )
    {
        log.info("Create Resource for studyId: {} for userId: {}",studyId,user.getUserId());
        Long userId = user.getUserId();
        resourceService.createResource(studyId,userId,resourceRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // 자료 상세 데이터 가져오기
    @GetMapping("/{resourceId}")
    public ResponseEntity<GetResourceDetailResponse> getResourceDetail(@PathVariable Long studyId, @PathVariable Long resourceId,
                                                                             @AuthenticationPrincipal CustomUserDetails user)
    {
        log.info("Get ResourceDetail for studyId: {} for resourceId: {}",studyId,resourceId);
        Long userId = user.getUserId();
        GetResourceDetailResponse result = resourceService.getResourceDetail(studyId, resourceId, userId);
        return ResponseEntity.ok(result);
    }

    // 자료 수정
    @PutMapping( path = "/{resourceId}",consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateResource(@PathVariable Long studyId, @PathVariable Long resourceId,
                               @AuthenticationPrincipal CustomUserDetails user
                            , @Valid @ModelAttribute UpdateResourceRequest updateResourceRequest)
    {
        log.info("Update ResourceDetail for studyId: {} for resourceId: {}",studyId,resourceId);
        Long userId = user.getUserId();
        resourceService.updateResource(studyId,resourceId,userId,updateResourceRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 자료 삭제
    @DeleteMapping("/{resourceId}")
    public  ResponseEntity<Void> deleteResource(@PathVariable Long studyId, @PathVariable Long resourceId,
                               @AuthenticationPrincipal CustomUserDetails user)
    {
        log.info("Delete Resource for studyId: {}, for resourceId: {}, for userId:{}",studyId,resourceId,user.getUserId());
        Long userId = user.getUserId();
        resourceService.deleteResource(studyId,resourceId,userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
