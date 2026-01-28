package com.planup.global.ui;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "현재 백엔드 서버가 정상 작동 중입니다! 🚀";
    }

    @GetMapping("/")
    public String main(@AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            return "로그인 성공! 환영합니다, " + user.getUsername() + "님! 🎉";
        }
        return "로그인 안 된 상태입니다.";
    }
}
