package com.study.focus.home.controller;

import com.study.focus.account.dto.CustomUserDetails;
import com.study.focus.home.dto.HomeResponse;
import com.study.focus.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 메인화면 데이터 가져오기
     */
    @GetMapping
    public ResponseEntity<HomeResponse> getHomeData(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(homeService.getHomeData(userId));
    }
}
