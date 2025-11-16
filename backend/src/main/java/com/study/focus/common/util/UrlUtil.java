package com.study.focus.common.util;

import org.springframework.web.util.UriComponentsBuilder;

public class UrlUtil {

    // ⭐️ 공통화된 리다이렉트 기본 경로 상수
    // 일반 로그인과 OAuth 로그인이 최종 리다이렉트되는 프론트엔드 URL입니다.
    public static final String FRONTEND_BASE_URL = "http://localhost:5173";
    public static final String BACKEND_BASE_URL = "http://localhost:8080";
    // 프론트 배포 완료시 변경 + 환경변수 설정
    public static final String FRONT_BEFO_URL = "http://my-study-frontend.s3-website-ap-northeast-2.amazonaws.com";
    // ⭐️ 공통화된 경로 상수
    public static final String HOME_PATH = "/home"; // OAuth와 일반 로그인 모두 이 경로를 사용
    public static final String PROFILE_SETUP_PATH = "/profile"; // OAuth와 일반 로그인 모두 이 경로를 사용

    /**
     * 프로필 존재 여부에 따라 AccessToken을 포함한 최종 리다이렉트 URL을 생성합니다.
     * (로직은 변경 없음)
     */
    public static String createRedirectUrl(
            String baseUrl,
            String homePath,
            String profileSetupPath,
            String accessToken,
            String refreshToken,
            boolean profileExists) {

        String path;

        if (profileExists) {
            path = homePath;
        } else {
            path = profileSetupPath;
        }

        return UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();
    }
}