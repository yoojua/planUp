package com.planup.config.auth;

import com.planup.config.jwt.JwtTokenProvider;
import com.planup.domain.user.Role;
import com.planup.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        log.info("OAuth2 User Attributes: {}", attributes);

        // 이메일 추출 (카카오/네이버)
        String email = extractEmail(attributes);

        // 1. Access Token 생성 (프론트 전달용)
        String accessToken = jwtTokenProvider.createAccessToken(email, Role.USER.getKey());

        // 2. Refresh Token 생성 (Redis 저장용, 프론트엔드에는 안 줌)
        // (원래는 쿠키로 내려줘야 '재발급'이 가능하지만, 일단 저장 로직 확인부터 합니다)
        jwtTokenProvider.createRefreshToken(email);

        log.info("로그인 성공! 이메일: {}, AccessToken 발급", email);

        // TODO: 배포시 도메인 변경해야 함
        response.sendRedirect("http://localhost:5173/oauth/callback?token=" + accessToken);

    }

    private String extractEmail(Map<String, Object> attributes) {
        // 1. 네이버 (attributes 안에 바로 "email"이 들어있음)
        if (attributes.containsKey("email")) {
            return String.valueOf(attributes.get("email"));
        }

        // 2. 카카오 (kakao_account 안에 email이 있음)
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount.containsKey("email")) {
                return String.valueOf(kakaoAccount.get("email"));
            }
        }


        log.warn("이메일을 찾을 수 없음. Attributes: {}", attributes);
        return "unknown_user"; // 이메일 못 찾았을 때 기본값
    }

}
