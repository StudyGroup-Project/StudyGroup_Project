package com.study.focus.chat.controller;

import com.study.focus.chat.dto.ChatMessageResponse;
import com.study.focus.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/chatMessages")
@RequiredArgsConstructor
public class ChatRestController { // 히스토리 조회용

    private final ChatService chatService;

    /**
     * 채팅 메시지 목록 조회
     * - lastMessageId 없으면 최근 limit개
     * - lastMessageId 있으면 해당 id 이전 메시지 기준으로 조회 (추후 커서 페이징 확장 가능)
     */
    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long studyId,
                                                                 @RequestParam(required = false) Long lastMessageId,
                                                                 @RequestParam(defaultValue = "50") int limit) {
        List<ChatMessageResponse> messages = chatService.getRecentMessages(studyId, lastMessageId, limit);
        return ResponseEntity.ok(messages);
    }
}
