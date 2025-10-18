package com.study.focus.notification.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.notification.dto.GetNotificationDetailResponse;
import com.study.focus.notification.dto.GetNotificationsListResponse;
import com.study.focus.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 가져오기
    @GetMapping
    public ResponseEntity<List<GetNotificationsListResponse>> getNotifications(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(notificationService.getNotifications(studyId,user.getUserId()));
    }

    // 알림 상세 데이터 가져오기
    @GetMapping("/{notificationId}")
    public ResponseEntity<GetNotificationDetailResponse>    getNotificationDetail(@PathVariable Long studyId, @PathVariable Long notificationId, @AuthenticationPrincipal CustomUserDetails user)
    {
        return ResponseEntity.ok(notificationService.getNotificationDetail(studyId,notificationId,user.getUserId()));
    }
}
