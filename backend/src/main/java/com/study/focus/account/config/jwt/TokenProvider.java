package com.study.focus.account.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String identifier) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiry());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .claim("identifier", identifier)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiry());

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getRefreshTokenExpiry() {
        return jwtProperties.getRefreshTokenExpiry();
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return parseClaims(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
