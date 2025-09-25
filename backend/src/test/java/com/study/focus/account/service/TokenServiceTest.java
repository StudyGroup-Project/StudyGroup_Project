package com.study.focus.account.service;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.*;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    @Mock private TokenProvider tokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private SystemCredentialRepository systemCredentialRepository;
    @Mock private OAuthCredentialRepository oAuthCredentialRepository;

    @InjectMocks
    private TokenService tokenService;

    private User systemUser;
    private User oauthUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        systemUser = User.builder()
                .id(1L)
                .loginType(LoginType.SYSTEM)
                .build();

        oauthUser = User.builder()
                .id(2L)
                .loginType(LoginType.OAUTH)
                .build();
    }

    @Test
    @DisplayName("토큰 생성 성공")
    void createToken_success() {
        when(tokenProvider.createAccessToken(1L, "identifier"))
                .thenReturn("access123");
        when(tokenProvider.createRefreshToken(1L))
                .thenReturn("refresh123");
        when(tokenProvider.getRefreshTokenExpiry()).thenReturn(3600000L);

        var response = tokenService.createToken(systemUser, "identifier");

        assertThat(response.getAccessToken()).isEqualTo("access123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh123");
        assertThat(response.getRefreshTokenExpiry()).isEqualTo(3600000L);
    }

    @Test
    @DisplayName("SYSTEM 유저 - RefreshToken 기반 AccessToken 재발급")
    void createNewAccessToken_systemUser() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh123")
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .user(systemUser)
                .build();

        SystemCredential credential = SystemCredential.builder()
                .loginId("sysId")
                .password("encodedPw")
                .user(systemUser)
                .build();

        when(refreshTokenService.findByRefreshToken("refresh123"))
                .thenReturn(refreshToken);
        when(systemCredentialRepository.findByUser(systemUser))
                .thenReturn(Optional.of(credential));
        when(tokenProvider.createAccessToken(1L, "sysId"))
                .thenReturn("newAccess123");

        String newAccessToken = tokenService.createNewAccessToken("refresh123");

        assertThat(newAccessToken).isEqualTo("newAccess123");
    }

    @Test
    @DisplayName("OAUTH 유저 - RefreshToken 기반 AccessToken 재발급")
    void createNewAccessToken_oauthUser() {
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh456")
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .user(oauthUser)
                .build();

        OAuthCredential credential = OAuthCredential.builder()
                .provider(Provider.GOOGLE)
                .providerUserId("google123")
                .user(oauthUser)
                .build();

        when(refreshTokenService.findByRefreshToken("refresh456"))
                .thenReturn(refreshToken);
        when(oAuthCredentialRepository.findByUser(oauthUser))
                .thenReturn(Optional.of(credential));
        when(tokenProvider.createAccessToken(2L, "GOOGLE:google123"))
                .thenReturn("newAccess456");

        String newAccessToken = tokenService.createNewAccessToken("refresh456");

        assertThat(newAccessToken).isEqualTo("newAccess456");
    }

    @Test
    @DisplayName("RefreshToken 만료 - 예외 발생")
    void createNewAccessToken_expiredToken() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired")
                .expiryDate(LocalDateTime.now().minusMinutes(1))
                .user(systemUser)
                .build();

        when(refreshTokenService.findByRefreshToken("expired"))
                .thenReturn(expired);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.createNewAccessToken("expired")
        );

        assertThat(ex.getMessage()).isEqualTo("Refresh Token이 만료되었습니다.");
    }
}
