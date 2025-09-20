package com.study.focus.home.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    // 메인화면 데이터 가져오기
    @GetMapping
    public void getHomeData() {}
}
