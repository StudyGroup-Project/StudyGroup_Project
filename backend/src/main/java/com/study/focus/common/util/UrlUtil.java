package com.study.focus.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlUtil {

    // ⭐️ 공통화된 리다이렉트 기본 경로 상수
    // 일반 로그인과 OAuth 로그인이 최종 리다이렉트되는 프론트엔드 URL입니다.
    public static final String FRONTEND_BASE_URL = "http://localhost:5173";
    public static final String BACKEND_BASE_URL = "http://localhost:8080";
    // 프론트 배포 완료시 변경 + 환경변수 설정
    public static final String FRONT_BEFO_URL = "http://my-study-focus-frontend.s3-website.ap-northeast-2.amazonaws.com";
    // ⭐️ 공통화된 경로 상수
    public static final String HOME_PATH = "/home"; // OAuth와 일반 로그인 모두 이 경로를 사용
    public static final String PROFILE_SETUP_PATH = "/profile"; // OAuth와 일반 로그인 모두 이 경로를 사용

    // ==========================================================
    // 1) 요청 Origin 기반으로 "현재 프론트 URL" 자동 감지
    // ==========================================================
    public static String detectFrontendBaseUrl(HttpServletRequest request) {

        // 1) Origin 우선
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }

        // 2) Origin 없으면 Referer 활용 (뒤에 path 제거)
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                // Referer 예: http://localhost:5173/login
                // → Base URL: http://localhost:5173
                return referer.split("/")[0] + "//" + referer.split("/")[2];
            } catch (Exception ignored) {}
        }

        // 3) fallback (배포 기본값)
        return FRONT_BEFO_URL;
    }

    // ==========================================================
    // 2) AccessToken 기반 최종 redirect URL 생성
    // ==========================================================
    public static String createRedirectUrl(
            String baseUrl,
            String homePath,
            String profileSetupPath,
            String accessToken,
            String refreshToken,
            boolean profileExists
    ) {

        String path = profileExists ? homePath : profileSetupPath;

        return UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .toUriString();
    }
}
