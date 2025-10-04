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

        // CONNECT 또는 SEND 시 토큰 유효성 검사
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
            }

            if (jwtToken == null || !tokenProvider.validateToken(jwtToken)) {
                log.warn("Invalid JWT Token: {}", jwtToken);
                throw new IllegalArgumentException("Invalid JWT Token");
            }
        }

        return message;
    }
}
