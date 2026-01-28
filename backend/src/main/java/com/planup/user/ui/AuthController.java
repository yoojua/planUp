package com.planup.user.ui;

import com.planup.global.dto.ApiResponse;
import com.planup.user.infrastructure.security.CustomUserDetails;
import com.planup.user.infrastructure.security.JwtTokenProvider;
import com.planup.user.application.AuthService;
import com.planup.user.ui.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
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
            redisTemplate.delete("RT:" + email);
        }
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 3. 자체 회원가입(중복 회원가입 방지)
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            authService.signup(request);
            return ResponseEntity.ok("회원가입 성공!"); // 200 OK
        } catch (IllegalArgumentException e) {
            // AuthService에서 던진 에러 메시지("이미 카카오로...")를 그대로 반환
            return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        }
    }

    // 4. 자체 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request);
            return ResponseEntity.ok(Map.of("accessToken", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("아이디 또는 비밀번호가 잘못되었습니다.");
        }
    }

    // 비밀번호 찾기 (임시 비번 발송)
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.resetPassword(email);
        return ApiResponse.success("이메일로 임시 비밀번호를 발송했습니다.");
    }

    // 내 정보 조회 (로그인 타입 확인용)
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.error("로그인 된 사용자가 아닙니다."); // 혹은 null 반환
        }
        return ApiResponse.success(new UserResponse(userDetails.getUser()));
    }

}