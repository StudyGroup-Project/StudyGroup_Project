package com.study.focus.account.config;

import com.study.focus.account.config.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            try {
                Claims claims = tokenProvider.getClaims(token);
                String userIdString = claims.getSubject(); // 토큰 Subject에서 userId 문자열 추출

                // 1. userId로 CustomUserDetails 객체를 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(userIdString);

                // 2. Authentication 객체 생성 시 UserDetails를 Principal로 사용
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, // CustomUserDetails (Principal)
                                null,        // Credential (토큰은 이미 검증되었으므로 null 사용)
                                userDetails.getAuthorities() // 권한 정보
                        );

                // 3. SecurityContext에 등록
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // 토큰은 유효했지만, 사용자 정보 로드에 실패한 경우
                log.warn("토큰은 유효하나 사용자 정보 로드 실패: {}", e.getMessage());
                // SecurityContext에 Authentication을 설정하지 않고 넘어가면,
                // 다음 필터에서 UNAUTHORIZED 처리가 되거나, 컨트롤러에서 null 주입됨.
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
