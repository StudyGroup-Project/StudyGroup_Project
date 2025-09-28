package com.study.focus.account.service;

import com.study.focus.account.domain.*;
import com.study.focus.account.dto.*;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.RefreshTokenRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import com.study.focus.account.repository.UserRepository;
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
    private final RefreshTokenService
            refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    // OAuth 로그인
    public LoginResponse oauthLogin(Provider provider, String providerUserId) {
        // Credential 확인
        OAuthCredential credential = oAuthCredentialRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> {
                    // 신규 사용자 → User 생성
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

        // identifier: "provider:providerId"
        String identifier = provider.name() + ":" + providerUserId;

        // JWT 발급
        TokenResponse tokenResponse = tokenService.createToken(user, identifier);

        // RefreshToken 저장
        refreshTokenService.saveOrUpdate(user, tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiry());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    // 회원가입
    public void register(RegisterRequest request) {
        if (systemCredentialRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 1. User 생성
        User user = User.builder()
                .loginType(LoginType.SYSTEM)
                .build();
        userRepository.save(user);

        // 2. SystemCredential 생성 (loginId, password)
        SystemCredential credential = SystemCredential.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .user(user)
                .build();
        systemCredentialRepository.save(credential);
    }

    // 아이디 중복 확인
    public boolean checkId(String loginId) {
        return !systemCredentialRepository.existsByLoginId(loginId);
    }

    // 일반 로그인
    public LoginResponse login(LoginRequest request) {
        SystemCredential credential = systemCredentialRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = credential.getUser();
        user.updateLastLoginAt();

        String identifier = credential.getLoginId();

        TokenResponse tokenResponse = tokenService.createToken(user, identifier);

        refreshTokenService.saveOrUpdate(user, tokenResponse.getRefreshToken(), tokenResponse.getRefreshTokenExpiry());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    // 로그아웃
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    // 로그아웃
    public void logoutByRefreshToken(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}