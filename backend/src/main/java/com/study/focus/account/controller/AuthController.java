package com.study.focus.account.controller;

import com.study.focus.account.domain.Provider;
import com.study.focus.account.dto.*;
import com.study.focus.account.service.AccountService;
import jakarta.servlet.http.Cookie;
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
    public void login(@ModelAttribute LoginRequest request, HttpServletResponse response) throws IOException {
        LoginResponse loginResponse = accountService.login(request);

        // RefreshToken 쿠키 저장
        Cookie cookie = new Cookie("refresh_token", loginResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 테스트는 false, 배포는 true
        cookie.setPath("/");
        cookie.setMaxAge((int) Duration.ofDays(14).getSeconds());
        response.addCookie(cookie);

        // AccessToken 쿼리 파라미터로 전달
        response.sendRedirect("/home.html?token=" + loginResponse.getAccessToken());
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
}
