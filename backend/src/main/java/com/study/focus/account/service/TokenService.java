package com.study.focus.account.service;

import com.study.focus.account.domain.LoginType;
import com.study.focus.account.domain.OAuthCredential;
import com.study.focus.account.domain.RefreshToken;
import com.study.focus.account.domain.User;
import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.dto.TokenResponse;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final SystemCredentialRepository systemCredentialRepository;
    private final OAuthCredentialRepository oAuthCredentialRepository;

    public TokenResponse createToken(User user, String identifier) {
        String accessToken = tokenProvider.createAccessToken(user.getId(), identifier);
        String refreshToken = tokenProvider.createRefreshToken(user.getId());

        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenProvider.getRefreshTokenExpiry()
        );
    }

    @Transactional
    public String createNewAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByRefreshToken(refreshTokenValue);

        if (refreshToken.isExpired()) {
            throw new BusinessException(UserErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        String identifier;
        if (user.getLoginType() == LoginType.SYSTEM) {
            identifier = systemCredentialRepository.findByUser(user)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.SYSTEM_CREDENTIAL_NOT_FOUND))
                    .getLoginId();
        } else {
            OAuthCredential credential = oAuthCredentialRepository.findByUser(user)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.OAUTH_CREDENTIAL_NOT_FOUND));
            identifier = credential.getProvider().name() + ":" + credential.getProviderUserId();
        }

        return tokenProvider.createAccessToken(user.getId(), identifier);
    }
}
