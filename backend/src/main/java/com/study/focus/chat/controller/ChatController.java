package com.study.focus.chat.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies/{studyId}/chatMessages")
public class ChatController {

    // 채팅 목록 가져오기
    @GetMapping
    public void getMessages(@PathVariable Long studyId,
                            @RequestParam(required = false) Long lastMessageId,
                            @RequestParam(defaultValue = "50") int limit) {}

    // 채팅하기
    @PostMapping
    public void sendMessage(@PathVariable Long studyId) {}
}
