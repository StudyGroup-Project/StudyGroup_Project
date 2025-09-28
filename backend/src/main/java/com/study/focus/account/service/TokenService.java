package com.study.focus.account.service;

import com.study.focus.account.domain.LoginType;
import com.study.focus.account.domain.OAuthCredential;
import com.study.focus.account.domain.RefreshToken;
import com.study.focus.account.domain.User;
import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.dto.TokenResponse;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final SystemCredentialRepository  systemCredentialRepository;
    private final OAuthCredentialRepository oAuthCredentialRepository;

    /**
     * 토큰 생성 (로그인 시 호출)
     */
    public TokenResponse createToken(User user, String identifier) {
        String accessToken = tokenProvider.createAccessToken(user.getId(), identifier);
        String refreshToken = tokenProvider.createRefreshToken(user.getId());

        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenProvider.getRefreshTokenExpiry()
        );
    }

    /**
     * RefreshToken 기반 AccessToken 재발급
     */
    @Transactional
    public String createNewAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByRefreshToken(refreshTokenValue);

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다.");
        }

        User user = refreshToken.getUser();

        // identifier 결정
        String identifier;
        if (user.getLoginType() == LoginType.SYSTEM) {
            identifier = systemCredentialRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("SystemCredential이 존재하지 않습니다."))
                    .getLoginId();
        } else {
            OAuthCredential credential = oAuthCredentialRepository.findByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("OAuthCredential이 존재하지 않습니다."));
            identifier = credential.getProvider().name() + ":" + credential.getProviderUserId();
        }

        return tokenProvider.createAccessToken(user.getId(), identifier);
    }
}
