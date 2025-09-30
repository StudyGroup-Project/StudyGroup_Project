package com.study.focus.account.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TokenProviderTest {

    private TokenProvider tokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        // application.yml 설정값 그대로 사용
        jwtProperties.setSecretKey("A4wZcrvhI3eV3YWa4auKBMUCx7ZHTpl0mTL2ngtoNHI=");
        jwtProperties.setIssuer("guscjf8921@gmail.com");
        jwtProperties.setAccessTokenExpiry(3600000L);       // 1시간
        jwtProperties.setRefreshTokenExpiry(1209600000L);   // 14일

        tokenProvider = new TokenProvider(jwtProperties);
        tokenProvider.init(); // @PostConstruct 수동 호출
    }

    @Test
    @DisplayName("AccessToken을 생성하고 유저아이디와 identifier를 추출할 수 있다")
    void createAccessTokenAndExtractUserId() {
        // given
        Long userId = 123L;
        String identifier = "system";

        // when
        String accessToken = tokenProvider.createAccessToken(userId, identifier);
        Long extractedUserId = tokenProvider.getUserIdFromToken(accessToken);
        Claims claims = tokenProvider.getClaims(accessToken);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(claims.get("identifier", String.class)).isEqualTo(identifier);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("RefreshToken을 생성하고 유저아이디를 추출할 수 있다")
    void createRefreshTokenAndExtractUserId() {
        // given
        Long userId = 456L;

        // when
        String refreshToken = tokenProvider.createRefreshToken(userId);
        Long extractedUserId = tokenProvider.getUserIdFromToken(refreshToken);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(tokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("JwtFactory로 생성한 토큰에서 subject를 읽을 수 있다")
    void createTokenWithJwtFactory() {
        // given
        JwtFactory jwtFactory = JwtFactory.builder()
                .subject("789") // subject는 String
                .build();

        // when
        String token = jwtFactory.createToken(jwtProperties);
        Claims claims = tokenProvider.getClaims(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("789");
    }

    @Test
    @DisplayName("잘못된 토큰은 validateToken이 false를 반환한다")
    void invalidTokenShouldReturnFalse() {
        // given
        String invalidToken = "this.is.not.a.valid.token";

        // when
        boolean result = tokenProvider.validateToken(invalidToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("AccessToken 만료시간이 설정대로 반영된다")
    void accessTokenExpiryIsAppliedCorrectly() {
        // given
        Long userId = 999L;
        String identifier = "expiry-test";

        // when
        String token = tokenProvider.createAccessToken(userId, identifier);
        Claims claims = tokenProvider.getClaims(token);

        long expectedExpiryMillis = jwtProperties.getAccessTokenExpiry();
        long actualExpiryMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // then
        assertThat(actualExpiryMillis).isEqualTo(expectedExpiryMillis);
    }

    @Test
    @DisplayName("RefreshToken 만료시간이 설정대로 반영된다")
    void refreshTokenExpiryIsAppliedCorrectly() {
        // given
        Long userId = 1000L;

        // when
        String token = tokenProvider.createRefreshToken(userId);
        Claims claims = tokenProvider.getClaims(token);

        long expectedExpiryMillis = jwtProperties.getRefreshTokenExpiry();
        long actualExpiryMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        // then
        assertThat(actualExpiryMillis).isEqualTo(expectedExpiryMillis);
    }

    @Test
    @DisplayName("토큰의 issuer 값은 설정한 issuer와 동일해야 한다")
    void tokenIssuerShouldMatch() {
        // given
        String token = tokenProvider.createAccessToken(999L, "issuer-test");

        // when
        Claims claims = tokenProvider.getClaims(token);

        // then
        assertThat(claims.getIssuer()).isEqualTo(jwtProperties.getIssuer());
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken이 false를 반환하고 getClaims는 예외를 발생시킨다")
    void expiredTokenShouldBeInvalid() {
        // given: 만료시간을 과거로 설정
        JwtFactory jwtFactory = JwtFactory.builder()
                .subject("111")
                .expiration(new Date(System.currentTimeMillis() - 1000)) // 이미 만료
                .build();

        String expiredToken = jwtFactory.createToken(jwtProperties);

        // when & then
        assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
        assertThatThrownBy(() -> tokenProvider.getClaims(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
