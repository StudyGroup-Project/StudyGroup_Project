package com.study.focus.account.controller;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.dto.*;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.AccountService;
import com.study.focus.common.util.UrlUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final TokenProvider tokenProvider;
    private final UserProfileRepository userProfileRepository;

    // OAuth 로그인 -> OAuth2SuccessHandler 확인

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        accountService.register(request);
        return ResponseEntity.ok().build();
    }

    // 아이디 중복 확인
    @GetMapping("/check-id")
    public ResponseEntity<CheckDuplicatedIdResponse> checkId(@RequestParam String loginId) {
        boolean available = accountService.checkId(loginId);
        return ResponseEntity.ok(new CheckDuplicatedIdResponse(available));
    }

    // 일반 로그인
    @PostMapping("/login")
    public void login(@RequestBody LoginRequest requestBody, HttpServletResponse response,
                      HttpServletRequest request) throws IOException {
        LoginResponse loginResponse = accountService.login(requestBody);
        String accessToken = loginResponse.getAccessToken();

        // RefreshToken 쿠키 저장
        Cookie cookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 테스트는 false, 배포는 true
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(14).getSeconds());
        response.addCookie(cookie);

        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        boolean profileExists = userProfileRepository.findByUserId(userId).isPresent();

        String baseUrl = getClientBaseUrl(request);

        String targetUrl = UrlUtil.createRedirectUrl(
                baseUrl,
                UrlUtil.HOME_PATH,
                UrlUtil.PROFILE_SETUP_PATH,
                accessToken,
                profileExists
        );

        response.sendRedirect(targetUrl);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refresh_token", required = false) String refreshToken,
                                       HttpServletResponse response) {
        if (refreshToken != null) {
            accountService.logoutByRefreshToken(refreshToken);

            // 쿠키 즉시 만료시키기
            ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true)
                    .secure(true) // 배포 환경에서는 true
                    .path("/")
                    .maxAge(0) // 즉시 만료
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 요청의 스키마와 호스트/포트를 기반으로 클라이언트가 접근한 기본 URL을 생성합니다.
     * (예: http://localhost:3000)
     */
    private String getClientBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();

        // ⭐️ Host 헤더를 사용하여 클라이언트가 요청한 호스트와 포트를 가져옵니다.
        // 일반 로그인 요청 시, 브라우저가 요청한 주소(localhost:3000 등)가 담겨옵니다.
        String hostPort = request.getHeader("Host");

        // 주의: 프록시(예: AWS ALB, Nginx)를 사용할 경우,
        // "Host" 헤더 대신 "X-Forwarded-Host" 헤더를 확인해야 할 수도 있습니다.
        // 하지만 일반적인 로컬 개발 환경에서는 Host로 충분합니다.

        return scheme + "://" + hostPort;
    }
}
