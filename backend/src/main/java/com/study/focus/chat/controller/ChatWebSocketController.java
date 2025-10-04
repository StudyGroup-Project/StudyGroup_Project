package com.study.focus.chat.controller;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.chat.dto.ChatMessageRequest;
import com.study.focus.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final TokenProvider tokenProvider;

    @MessageMapping("/studies/{studyId}") // 클라이언트 → /pub/studies/{studyId}
    public void sendMessage(@DestinationVariable Long studyId,
                            ChatMessageRequest request,
                            @Header("Authorization") String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("JWT 토큰 없음 또는 잘못됨");
        }

        String jwt = token.substring(7);
        Long userId = tokenProvider.getUserIdFromToken(jwt);

        log.info("웹소켓 수신: studyId={}, userId={}, content={}", studyId, userId, request.getContent());

        chatService.handleMessage(studyId, userId, request.getContent());
    }
}