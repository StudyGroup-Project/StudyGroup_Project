package com.study.focus.chat.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    // 채팅 목록 가져오기
    public void getMessages(Long studyId, Long lastMessageId, int limit) {
        // TODO: 특정 스터디의 채팅 목록 조회 (페이징/스크롤 방식)
    }

    // 채팅하기
    public void sendMessage(Long studyId) {
        // TODO: 채팅 메시지 저장
    }
}

