package com.study.focus.account.service;

import com.study.focus.account.domain.RefreshToken;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.RefreshTokenRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
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
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder().id(1L).build();
    }

    @Test
    @DisplayName("RefreshToken 저장 및 업데이트")
    void saveOrUpdate_newAndUpdate() {
        when(refreshTokenRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        refreshTokenService.saveOrUpdate(user, "token123", 1000L);

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken 조회 실패 - 잘못된 토큰")
    void findByRefreshToken_fail() {
        when(refreshTokenRepository.findByToken("wrong")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.findByRefreshToken("wrong")
        );

        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.REFRESH_TOKEN_INVALID);
    }
}
