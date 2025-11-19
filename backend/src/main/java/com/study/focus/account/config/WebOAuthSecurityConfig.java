package com.study.focus.account.config;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.study.focus.account.config.oauth.OAuth2SuccessHandler;
import com.study.focus.account.config.oauth.OAuth2UserCustomService;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.AccountService;
import com.study.focus.common.util.UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserProfileRepository userProfileRepository;

    /* ----------------------------------------------------
     * 1) HTTP API 전용 CORS 설정 (WebSocket 제외)
     * ---------------------------------------------------- */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                UrlUtil.FRONT_BEFO_URL,
                "http://localhost:5173",
                "http://localhost:5174",
                "https://study-group-project-frontend.vercel.app"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // ★ 중요: WebSocket 경로는 CORS 적용하지 않는다
        source.registerCorsConfiguration("/api/**", config);

        return source;
    }

    /* ----------------------------------------------------
     * 2) static 리소스는 security에서 제외
     * ---------------------------------------------------- */
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().requestMatchers(
                new AntPathRequestMatcher("/img/**"),
                new AntPathRequestMatcher("/css/**"),
                new AntPathRequestMatcher("/js/**")
        );
    }

    /* ----------------------------------------------------
     * 3) Security Filter Chain
     * ---------------------------------------------------- */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // HTTP API CORS
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* ----------------------------------------------------
                 *  WebSocket 업그레이드 요청 허용
                 * ---------------------------------------------------- */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws-stomp/**").permitAll()  // ★ WebSocket 필수
                        .requestMatchers("/api/auth/login", "/api/auth/register",
                                "/api/auth/token", "/api/auth/logout",
                                "/api/auth/check-id").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                        .anyRequest().permitAll()
                )

                /* ----------------------------------------------------
                 *  JWT & OAuth2 Login
                 * ---------------------------------------------------- */
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(
                                        oAuth2AuthorizationRequestBasedOnCookieRepository()))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserCustomService))
                        .successHandler(oAuth2SuccessHandler)
                )

                /* ----------------------------------------------------
                 *  Authentication 실패 시 (API 요청만 적용)
                 * ---------------------------------------------------- */
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")
                ));

        return http.build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(AccountService accountService) {
        return new OAuth2SuccessHandler(
                accountService,
                userProfileRepository,
                tokenProvider,
                oAuth2AuthorizationRequestBasedOnCookieRepository()
        );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, userDetailsService);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
