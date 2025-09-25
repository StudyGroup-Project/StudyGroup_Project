package com.study.focus.account.config.oauth;

import com.study.focus.account.dto.TokenResponse;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long userId;  // User 전체 대신 PK만 보관
    private final Map<String, Object> attributes;
    private final TokenResponse tokenResponse;

    public CustomOAuth2User(Long userId, Map<String, Object> attributes, TokenResponse tokenResponse) {
        this.userId = userId;
        this.attributes = attributes;
        this.tokenResponse = tokenResponse;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_USER");
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}