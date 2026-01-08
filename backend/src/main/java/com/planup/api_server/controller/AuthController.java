package com.planup.api_server.controller;

import com.planup.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    // 1. 토큰 재발급 (Access Token 만료 시 호출)
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // Refresh Token 검증 및 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    // 2. 로그아웃 (Redis에서 Refresh Token 삭제)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            // Redis에서 해당 이메일의 Refresh Token 삭제
            redisTemplate.delete("RT:" + email);
        }
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}