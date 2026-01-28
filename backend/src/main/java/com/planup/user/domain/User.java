package com.planup.user.domain;

import com.planup.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // DB 예약어 'user'와 겹칠 수 있어서 'users'로 지정
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 이메일 (로그인 ID 역할)

    @Column(nullable = false)
    private String nickname; // 사용자 이름

    private String password;

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING) // DB에 "USER", "ADMIN" 문자열로 저장
    @Column(nullable = false)
    private Role role; // "ROLE_USER", "ROLE_ADMIN"

    @Enumerated(EnumType.STRING) // DB에 "KAKAO", "NAVER", "LOCAL" 문자열로 저장
    private SocialType socialType;

    private String socialId; // 소셜 서비스 식별값 (네이버: id, 카카오: id)

    @Builder
    public User(String email, String nickname, String password, String profileImageUrl, Role role, SocialType socialType, String socialId) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    // OAuth 서비스
    // 1. 회원정보 수정 (소셜 계정의 닉네임이 바뀌었을 때 업데이트)
    public User update(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    // 2. 권한 키 반환
    public String getRoleKey() {
        return this.role.getKey();
    }

    // 비밀번호 변경 (비밀번호 찾기 or 마이페이지 변경용)
    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }
}