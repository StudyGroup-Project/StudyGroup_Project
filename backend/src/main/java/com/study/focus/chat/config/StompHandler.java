package com.study.focus.chat.config;

import com.study.focus.account.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();
        log.info("[STOMP] command={} sessionId={}", command, accessor.getSessionId());

        // 1) command 없는 경우(handshake 등) → 그대로 통과
        if (command == null) {
            return message;
        }

        // 토큰 꺼내는 공통 함수
        String jwtToken = resolveToken(accessor);

        // 2) CONNECT에서 JWT 검증
        if (StompCommand.CONNECT.equals(command)) {
            log.info("[STOMP] CONNECT 헤더들: {}", accessor.toNativeHeaderMap());

            if (jwtToken == null) {
                log.warn("[STOMP] CONNECT - Authorization 헤더 없음, 연결 거부");
                // 실제 운영에서는 예외 던져서 끊어도 되지만,
                // 일단 원인 파악을 위해 null 리턴(메시지 drop)만 하거나 그대로 통과해도 됨
                // throw new IllegalArgumentException("Missing JWT in CONNECT");
                return null; // 이 세션 CONNECT는 처리 안 함 → 클라이언트에서 close로 보임
            }

            if (!tokenProvider.validateToken(jwtToken)) {
                log.warn("[STOMP] CONNECT - 유효하지 않은 토큰");
                return null;
            }

            log.info("[STOMP] CONNECT - JWT 검증 성공");
        }

        // 3) SEND에서도 JWT 검증
        if (StompCommand.SEND.equals(command)) {
            log.info("[STOMP] SEND dest={} headers={}",
                    accessor.getDestination(), accessor.toNativeHeaderMap());

            if (jwtToken == null || !tokenProvider.validateToken(jwtToken)) {
                log.warn("[STOMP] SEND - JWT 없음 또는 유효하지 않음");
                return null;
            }
        }

        return message;
    }

    /**
     * STOMP CONNECT / SEND native header에서 JWT 토큰 추출
     */
    private String resolveToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null) {
            header = accessor.getFirstNativeHeader("authorization");
        }

        if (header == null) {
            return null;
        }

        // "Bearer xxx" 형태면 제거
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
