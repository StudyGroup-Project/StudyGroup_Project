package com.study.focus.account.service;

import com.study.focus.account.domain.*;
import com.study.focus.account.dto.LoginRequest;
import com.study.focus.account.dto.LoginResponse;
import com.study.focus.account.dto.RegisterRequest;
import com.study.focus.account.dto.TokenResponse;
import com.study.focus.account.repository.OAuthCredentialRepository;
import com.study.focus.account.repository.RefreshTokenRepository;
import com.study.focus.account.repository.SystemCredentialRepository;
import com.study.focus.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SystemCredentialRepository systemCredentialRepository;
    @Mock private OAuthCredentialRepository oAuthCredentialRepository;
    @Mock private TokenService tokenService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("시스템 로그인 성공 - 올바른 아이디/비밀번호")
    void login_success() {
        // given
        User user = User.builder().id(1L).loginType(LoginType.SYSTEM).build();
        SystemCredential credential = SystemCredential.builder()
                .user(user)
                .loginId("testId")
                .password("encodedPw")
                .build();

        when(systemCredentialRepository.findByLoginId("testId"))
                .thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("rawPw", "encodedPw")).thenReturn(true);
        when(tokenService.createToken(any(), any()))
                .thenReturn(new TokenResponse("access", "refresh", 1000L));

        // when
        LoginResponse response = accountService.login(
                new LoginRequest("testId", "rawPw")
        );

        // then
        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        verify(refreshTokenService).saveOrUpdate(eq(user), eq("refresh"), any());
    }

    @Test
    @DisplayName("시스템 로그인 실패 - 아이디 없음")
    void login_fail_notFound() {
        when(systemCredentialRepository.findByLoginId("wrongId"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.login(new LoginRequest("wrongId", "pw"))
        );

        assertThat(ex.getMessage()).isEqualTo("존재하지 않는 아이디입니다.");
    }

    @Test
    @DisplayName("시스템 로그인 실패 - 비밀번호 불일치")
    void login_fail_invalidPassword() {
        User user = User.builder().id(1L).build();
        SystemCredential credential = SystemCredential.builder()
                .user(user)
                .loginId("id")
                .password("encodedPw")
                .build();

        when(systemCredentialRepository.findByLoginId("id"))
                .thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.login(new LoginRequest("id", "wrongPw"))
        );

        assertThat(ex.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void register_fail_duplicateId() {
        when(systemCredentialRepository.existsByLoginId("dupId")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("dupId", "pw");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(request)
        );

        assertThat(ex.getMessage()).isEqualTo("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("OAuth 로그인 신규 유저 생성")
    void oauthLogin_newUser() {
        when(oAuthCredentialRepository.findByProviderAndProviderUserId(Provider.GOOGLE, "123"))
                .thenReturn(Optional.empty());

        User newUser = User.builder().id(1L).loginType(LoginType.OAUTH).build();
        OAuthCredential newCred = OAuthCredential.builder()
                .id(10L)
                .user(newUser)
                .provider(Provider.GOOGLE)
                .providerUserId("123")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(oAuthCredentialRepository.save(any(OAuthCredential.class))).thenReturn(newCred);
        when(tokenService.createToken(any(), any()))
                .thenReturn(new TokenResponse("access", "refresh", 1000L));

        LoginResponse response = accountService.oauthLogin(Provider.GOOGLE, "123");

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        verify(refreshTokenService).saveOrUpdate(eq(newUser), eq("refresh"), any());
    }

    @Test
    @DisplayName("logout(userId) 호출 시 RefreshTokenService.deleteByUserId 실행")
    void logout_byUserId() {
        Long userId = 100L;

        // when
        accountService.logout(userId);

        // then
        verify(refreshTokenService, times(1)).deleteByUserId(userId);
    }

    @Test
    @DisplayName("logoutByRefreshToken(refreshToken) - 토큰 존재하면 삭제")
    void logoutByRefreshToken_found() {
        RefreshToken rt = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken("validRefresh"))
                .thenReturn(Optional.of(rt));

        accountService.logoutByRefreshToken("validRefresh");

        verify(refreshTokenRepository, times(1)).delete(rt);
    }

    @Test
    @DisplayName("logoutByRefreshToken(refreshToken) - 토큰 없으면 아무 일도 없음")
    void logoutByRefreshToken_notFound() {
        when(refreshTokenRepository.findByToken("missingRefresh"))
                .thenReturn(Optional.empty());

        accountService.logoutByRefreshToken("missingRefresh");

        verify(refreshTokenRepository, never()).delete(any());
    }
}
