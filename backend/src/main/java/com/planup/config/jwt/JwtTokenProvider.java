package com.planup.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    // 기능 1. 토큰 만들기
    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;
    private final RedisTemplate<String, String> redisTemplate;

    // 유효시간: Access(30분), Refresh(7일)
    private final long ACCESS_TOKEN_VALID_TIME = 30 * 60 * 1000L;
    private final long REFRESH_TOKEN_VALID_TIME = 7 * 24 * 60 * 60 * 1000L;

    @PostConstruct
    protected void init() {
        // secretKey를 디코딩해 실제 키로 변환
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성(로그인 성공시 호출됨)
    public String createAccessToken(String email, String role) {
        return createToken(email, role, ACCESS_TOKEN_VALID_TIME);
    }

    // Refresh Token 생성 및 Redis 저장
    public String createRefreshToken(String email) {
        String refreshToken = createToken(email, "ROLE_USER", REFRESH_TOKEN_VALID_TIME);

        redisTemplate.opsForValue()
                .set("RT:" + email, refreshToken, REFRESH_TOKEN_VALID_TIME, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    private String createToken(String email, String role, long validTime) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validTime)) // 토큰 만료시간
                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
                .compact();
    }

    // 기능 2. 토큰 검증
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);

        UserDetails principal = new User(claims.getSubject(), "",
                Collections.singleton(new SimpleGrantedAuthority(role)));

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Redis에서 Refresh Token 확인
    public boolean existsRefreshToken(String email) {
        return redisTemplate.opsForValue().get("RT:" + email) != null;
    }

    // Access Token 재발급 (Refresh Token 검증 후 실행)
    public String reissueAccessToken(String refreshToken) {
        // 토큰에서 이메일 추출
        String email = getClaims(refreshToken).getSubject();

        // Redis에 저장된 Refresh Token 가져오기
        String redisRT = (String) redisTemplate.opsForValue().get("RT:" + email);

        // 토큰이 일치하지 않거나 없으면 에러
        if (redisRT == null || !redisRT.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        return createAccessToken(email, "ROLE_USER");
    }

    // 내부적으로 토큰 해석하는 메서드
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
