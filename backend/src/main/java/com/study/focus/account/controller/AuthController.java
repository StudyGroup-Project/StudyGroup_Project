
package com.study.focus.account.controller;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.domain.Provider;
import com.study.focus.account.dto.*;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.AccountService;
import com.study.focus.common.util.CookieUtil;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);

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

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String loginId = body.get("loginId");
        String password = body.get("password");

        LoginResponse loginResponse = accountService.login(new LoginRequest(loginId, password));

        String accessToken = loginResponse.getAccessToken();
        String refreshToken = loginResponse.getRefreshToken();

        // Refresh Token 쿠키 저장
        addRefreshTokenToCookie(httpRequest, httpResponse, refreshToken);

        // 프로필 존재 여부 체크
        Long userId = tokenProvider.getUserIdFromToken(accessToken);
        boolean profileExists = userProfileRepository.findByUserId(userId).isPresent();

        // 프론트가 이동할 redirect URL 생성
        String redirectUrl = UrlUtil.createRedirectUrl(
                UrlUtil.FRONTEND_BASE_URL,
                UrlUtil.HOME_PATH,
                UrlUtil.PROFILE_SETUP_PATH,
                accessToken,
                refreshToken,
                profileExists
        );

        // JSON 응답
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("profileExists", profileExists);
        result.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(result);
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

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }
}
