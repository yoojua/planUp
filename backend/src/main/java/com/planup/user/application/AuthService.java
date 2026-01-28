package com.planup.user.application;

import com.planup.user.infrastructure.security.JwtTokenProvider;
import com.planup.user.domain.Role;
import com.planup.user.domain.SocialType;
import com.planup.user.domain.User;
import com.planup.user.ui.LoginRequest;
import com.planup.user.ui.SignupRequest;
import com.planup.user.domain.UserRepository;
import com.planup.global.exception.CustomException;
import com.planup.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 자체 회원가입
    @Transactional
    public Long signup(SignupRequest request) {

        // 이메일 중복 검사 및 가입 경로 확인
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (existingUser.getSocialType() == SocialType.KAKAO) {
                throw new IllegalArgumentException("이미 카카오로 가입된 이메일입니다. 카카오 로그인을 이용해주세요.");
            } else if (existingUser.getSocialType() == SocialType.NAVER) {
                throw new IllegalArgumentException("이미 네이버로 가입된 이메일입니다. 네이버 로그인을 이용해주세요.");
            } else {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }
        });

        // 저장 (비밀번호 암호화 필수)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .socialType(SocialType.LOCAL) // 자체 가입
                .role(Role.USER)              // 기본 권한
                .build();

        return userRepository.save(user).getId();
    }

    // 2. 자체 로그인
    @Transactional
    public String login(LoginRequest request) {
        // 1. 이메일 존재 여부 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 소셜 로그인으로 가입된 계정인지 확인 (비밀번호가 없을 수 있음)
        if (user.getSocialType() != SocialType.LOCAL) {
            throw new CustomException(ErrorCode.LOGIN_FAILURE); // "소셜 로그인 계정입니다" 메시지를 따로 만들어도 됨
        }

        // 3. 비밀번호 검증 (입력 비번 vs DB 암호화 비번)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILURE);
        }

        // 4.. Refresh Token 생성 및 Redis 저장 (로그인 유지용)
        jwtTokenProvider.createRefreshToken(user.getEmail());

        // 4. Access Token 생성 후 반환
        return jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoleKey());
    }

    // 3. 비밀번호 재설정 요청 (비밀번호 찾기)
    @Transactional
    public void resetPassword(String email) {
        // 1. 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 소셜 로그인 유저인지 확인 (핵심 로직 ⭐)
        if (user.getSocialType() != SocialType.LOCAL) {
            // 예외 메시지로 "카카오 계정입니다" 등을 알려줌
            throw new IllegalArgumentException("해당 계정은 " + user.getSocialType() + " 소셜 로그인 계정입니다. 소셜 로그인을 이용해주세요.");
        }

        // 3. (자체 회원인 경우) 임시 비밀번호 생성 및 이메일 전송 로직
        String tempPassword = generateTempPassword(); // 임시 비번 생성 메서드 (아래 참고)

        // 4. DB 업데이트 (암호화해서 저장)
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // 5. 이메일 전송 (가상 코드)
        // mailService.sendPasswordResetMail(user.getEmail(), tempPassword);
        System.out.println("이메일 발송: " + user.getEmail() + " / 임시비번: " + tempPassword);
    }

    // 임시 비밀번호 생성 헬퍼 (랜덤 8자리)
    private String generateTempPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}