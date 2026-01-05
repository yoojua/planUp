package com.planup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/hello", "/").permitAll() // "/hello"와 "/"는 로그인 없이 허용
                        .anyRequest().authenticated()               // 그 외 모든 요청은 로그인 필요
                )
                .formLogin(login -> login.disable())            // 기본 로그인 폼 끄기 (API 서버니까)
                .csrf(csrf -> csrf.disable());                  // CSRF 보안 끄기 (개발 편의상)

        return http.build();
    }
}