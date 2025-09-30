package com.study.focus.account.service;

import com.study.focus.account.domain.RefreshToken;
import com.study.focus.account.domain.User;
import com.study.focus.account.repository.RefreshTokenRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveOrUpdate(User user, String token, Long expiryMillis) {
        LocalDateTime expiryDate = LocalDateTime.now().plus(Duration.ofMillis(expiryMillis));

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        rt -> rt.update(token, expiryDate),
                        () -> {
                            RefreshToken newToken = RefreshToken.builder()
                                    .token(token)
                                    .expiryDate(expiryDate)
                                    .user(user)
                                    .build();
                            refreshTokenRepository.save(newToken);
                        }
                );
    }

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(UserErrorCode.REFRESH_TOKEN_INVALID));
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
