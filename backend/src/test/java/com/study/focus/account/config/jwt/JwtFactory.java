package com.study.focus.account.config.jwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Getter;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Getter
public class JwtFactory {

    private String subject = "1"; // 기본값: userId = 1
    private Date issuedAt = new Date();
    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = emptyMap();

    @Builder
    public JwtFactory(String subject, Date issuedAt, Date expiration,
                      Map<String, Object> claims) {
        this.subject = (subject != null) ? subject : this.subject;
        this.issuedAt = (issuedAt != null) ? issuedAt : this.issuedAt;
        this.expiration = (expiration != null) ? expiration : this.expiration;
        this.claims = (claims != null) ? claims : this.claims;
    }

    public static JwtFactory withDefaultValues() {
        return JwtFactory.builder().build();
    }

    public String createToken(JwtProperties jwtProperties) {
        // HS256 서명 키 준비
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setSubject(subject) // userId 같은 값
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
