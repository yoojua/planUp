package com.planup.user.infrastructure.auth;

import com.planup.user.domain.Role;
import com.planup.user.infrastructure.security.CustomUserDetails;
import com.planup.user.infrastructure.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        // 1. 로그인 된 유저 정보 가져오기
        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();
        String email = oAuth2User.getUsername();
        String role = oAuth2User.getUser().getRoleKey(); // ROLE_USER

        // 2. 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 3. 리다이렉트 (토큰을 URL에 담아서 프론트/클라이언트로 보냄)
        // 실제 배포 시에는 프론트엔드 주소로 변경해야 함
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/") // 일단 메인으로 이동
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}