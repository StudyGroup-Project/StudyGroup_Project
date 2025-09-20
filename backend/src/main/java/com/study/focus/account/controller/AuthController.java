package com.study.focus.account.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // OAuth 로그인
    @PostMapping("/oauth/{provider}")
    public void oauthLogin(@PathVariable String provider) {}

    // 회원가입
    @PostMapping("/register")
    public void register() {}

    // 아이디 중복 확인
    @GetMapping("/check-id")
    public void checkId(@RequestParam String loginId) {}

    // 일반 로그인
    @PostMapping("/login")
    public void login() {}

    // 로그아웃
    @PostMapping("/logout")
    public void logout() {}
}