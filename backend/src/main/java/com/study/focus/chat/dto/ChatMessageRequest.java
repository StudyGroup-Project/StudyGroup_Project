package com.study.focus.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // 역직렬화용 (JSON -> 객체)
@AllArgsConstructor
public class ChatMessageRequest {
    private String content;
}
