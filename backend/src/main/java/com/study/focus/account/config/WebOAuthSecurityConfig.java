package com.study.focus.account.config;

import com.study.focus.account.config.jwt.TokenProvider;
import com.study.focus.account.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.study.focus.account.config.oauth.OAuth2SuccessHandler;
import com.study.focus.account.config.oauth.OAuth2UserCustomService;
import com.study.focus.account.repository.UserProfileRepository;
import com.study.focus.account.service.AccountService;
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

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService; // UserDetailsService 의존성 추가
    private final UserProfileRepository userProfileRepository;

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        new AntPathRequestMatcher("/img/**"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**")
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
        return http
                .cors(Customizer.withDefaults()) // 🔥 CORS 활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/token", "/api/auth/logout", "/api/auth/check-id").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository()))
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        ))
                .build();
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
        return new TokenAuthenticationFilter(tokenProvider, userDetailsService); // userDetailsService 전달
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
