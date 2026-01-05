package com.planup.api_server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "현재 백엔드 서버가 정상 작동 중입니다! 🚀";
    }
}
