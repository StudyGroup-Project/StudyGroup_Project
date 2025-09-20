package com.study.focus.notification.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/notifications")
public class NotificationController {

    // 알림 목록 가져오기
    @GetMapping
    public void getNotifications(@PathVariable Long studyId) {}

    // 알림 상세 데이터 가져오기
    @GetMapping("/{notificationId}")
    public void getNotificationDetail(@PathVariable Long studyId, @PathVariable Long notificationId) {}
}
