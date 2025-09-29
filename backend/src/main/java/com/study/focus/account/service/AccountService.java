package com.study.focus.account.service;

import com.study.focus.account.domain.*;
import com.study.focus.account.dto.*;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.RefreshTokenRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import com.study.focus.account.repository.UserRepository;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final UserRepository userRepository;
    private final SystemCredentialRepository systemCredentialRepository;
    private final OAuthCredentialRepository oAuthCredentialRepository;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse oauthLogin(Provider provider, String providerUserId) {
        OAuthCredential credential = oAuthCredentialRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .loginType(LoginType.OAUTH)
                            .build();
                    userRepository.save(newUser);

                    OAuthCredential newCredential = OAuthCredential.builder()
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .user(newUser)
                            .build();
                    return oAuthCredentialRepository.save(newCredential);
                });

        User user = credential.getUser();
        user.updateLastLoginAt();

        String identifier = provider.name() + ":" + providerUserId;

        TokenResponse tokenResponse = tokenService.createToken(user, identifier);
        refreshTokenService.saveOrUpdate(user, tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiry());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    public void register(RegisterRequest request) {
        if (systemCredentialRepository.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID);
        }

        User user = User.builder()
                .loginType(LoginType.SYSTEM)
                .build();
        userRepository.save(user);

        SystemCredential credential = SystemCredential.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .user(user)
                .build();
        systemCredentialRepository.save(credential);
    }

    public boolean checkId(String loginId) {
        return !systemCredentialRepository.existsByLoginId(loginId);
    }

    public LoginResponse login(LoginRequest request) {
        SystemCredential credential = systemCredentialRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        User user = credential.getUser();
        user.updateLastLoginAt();

        String identifier = credential.getLoginId();

        TokenResponse tokenResponse = tokenService.createToken(user, identifier);
        refreshTokenService.saveOrUpdate(user, tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiry());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    public void logoutByRefreshToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
