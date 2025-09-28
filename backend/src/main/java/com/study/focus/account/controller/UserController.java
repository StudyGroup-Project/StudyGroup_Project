package com.study.focus.account.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.account.dto.GetMyProfileResponse;
import com.study.focus.account.dto.InitUserProfileRequest;
import com.study.focus.account.dto.UpdateUserProfileRequest;
import com.study.focus.account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 초기 프로필 설정 (이미지 제외)
    @PostMapping("/basic")
    public ResponseEntity<Void> initProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid InitUserProfileRequest request) {
        userService.initProfile(user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 프로필 이미지 설정 (등록/변경)
    @PostMapping("/image")
    public ResponseEntity<String> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestPart("file") MultipartFile file) {
        String imageUrl = userService.setProfileImage(user.getUserId(), file);
        return ResponseEntity.ok(imageUrl);
    }

    // 내 프로필 수정 (이미지 제외)
    @PatchMapping
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody UpdateUserProfileRequest request) {
        userService.updateProfile(user.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    // 내 프로필 정보 가져오기
    @GetMapping
    public ResponseEntity<GetMyProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        GetMyProfileResponse response = userService.getMyProfile(user.getUserId());
        return ResponseEntity.ok(response);
    }
}