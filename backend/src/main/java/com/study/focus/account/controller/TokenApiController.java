package com.study.focus.account.controller;

import com.study.focus.account.dto.CreateAccessTokenRequest;
import com.study.focus.account.dto.CreateAccessTokenResponse;
import com.study.focus.account.dto.TokenRefreshRequest;
import com.study.focus.account.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/api/auth/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(
            // 1. 쿠키를 필수가 아닌 선택사항으로 변경 (required = false)
            @CookieValue(name = "refresh_token", required = false) String refreshTokenFromCookie,
            // 2. 요청 본문도 선택적으로 받도록 추가 (required = false)
            @RequestBody(required = false) TokenRefreshRequest request) {

        String refreshToken = null;

        // 3. 쿠키에 리프레시 토큰이 있는지 먼저 확인
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isEmpty()) {
            refreshToken = refreshTokenFromCookie;
        }
        // 4. 쿠키에 없다면, 요청 본문에 있는지 확인
        else if (request != null && request.getRefreshToken() != null) {
            refreshToken = request.getRefreshToken();
        }

        // 5. 두 곳 모두 토큰이 없다면 에러 응답
        if (refreshToken == null) {
            // 400 Bad Request 에러 반환
            return ResponseEntity.badRequest().build();
        }

        // 6. 찾은 토큰으로 새로운 액세스 토큰 생성
        String newAccessToken = tokenService.createNewAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateAccessTokenResponse(newAccessToken));
    }
}
