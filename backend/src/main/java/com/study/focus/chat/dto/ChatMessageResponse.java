package com.study.focus.chat.dto;

import com.study.focus.account.domain.UserProfile;
import com.study.focus.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;
}