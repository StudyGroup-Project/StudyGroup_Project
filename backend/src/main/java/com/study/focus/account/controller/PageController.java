package com.study.focus.account.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/home")
    public String home() {
        // static/home.html 로 forward
        return "forward:/home.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/profile.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/")
    public String start() {
        // 초기에 토큰 있으면 바로 로그인 + home으로
        // 아니면 login으로
        return "forward:/login.html";
    }
}