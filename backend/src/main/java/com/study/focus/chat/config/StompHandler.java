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

        // 1) command가 없는 경우 (handshake 단계) → 무조건 통과
        if (accessor.getCommand() == null) {
            return message;
        }

        // 2) CONNECT 프레임일 때만 JWT 검사
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }

            if (jwtToken == null || !tokenProvider.validateToken(jwtToken)) {
                throw new IllegalArgumentException("Invalid or missing JWT Token in CONNECT");
            }
        }

        // 3) SEND 프레임일 때도 JWT 검사 (CONNECT 검증 통과한 뒤에만 도달)
        if (StompCommand.SEND.equals(accessor.getCommand())) {

            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }

            if (jwtToken == null || !tokenProvider.validateToken(jwtToken)) {
                throw new IllegalArgumentException("Invalid JWT Token in SEND");
            }
        }

        return message;
    }
}
