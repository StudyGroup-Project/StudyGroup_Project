package com.study.focus.account.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/profile")
public class UserController {

    // 초기 프로필 설정
    @PostMapping
    public void createProfile() {}

    // 내 프로필 조회
    @GetMapping
    public void getMyProfile() {}

    // 내 프로필 수정
    @PutMapping
    public void updateMyProfile() {}
}
