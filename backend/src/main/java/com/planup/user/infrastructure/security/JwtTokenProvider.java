package com.planup.user.infrastructure.security;

import com.planup.global.exception.CustomException;
import com.planup.global.exception.ErrorCode;
import com.planup.user.domain.User;
import com.planup.user.domain.UserRepository;
import com.planup.user.infrastructure.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String salt;
    private Key secretKey;

    // 만료시간: 1시간
    private final long exp = 1000L * 60 * 60;

    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository; // Redis 확인용
    private final UserRepository userRepository; // Role 확인용


    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(salt.getBytes(StandardCharsets.UTF_8));
    }

    // 1. 토큰 생성 (기존 유지)
    public String createAccessToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + exp))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성 (기존 유지)
    public String createRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 1000L * 60 * 60 * 24 * 14))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. 인증 정보 조회
    // 토큰에서 정보를 꺼내서 SecurityContext에 저장할 Authentication 객체를 생성
    public Authentication getAuthentication(String token) {
        // DB에서 유저 정보를 가져와서 CustomUserDetails 객체로 만듦
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(this.getEmail(token));

        // Authentication 객체에 CustomUserDetails를 담아서 리턴!
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 3. 토큰에서 회원 정보(이메일) 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 4. Request의 Header에서 토큰 가져오기 "Authorization: Bearer <token>"
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 5. 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // 6. 토큰 재발급
    public String reissueAccessToken(String refreshToken) {
        // 1. 리프레시 토큰 유효성 검사 (만료 여부 등)
        if (!validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN); // 혹은 적절한 예외
        }

        // 2. Redis에 저장된 토큰인지 확인 (로그아웃 되면 Redis에서 사라짐)
        // Redis의 Key는 refreshToken 자체입니다.
        refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 3. 유저 정보 가져오기 (Role이 필요해서)
        String email = getEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 4. 새 Access Token 발급
        return createAccessToken(email, user.getRoleKey());
    }
}