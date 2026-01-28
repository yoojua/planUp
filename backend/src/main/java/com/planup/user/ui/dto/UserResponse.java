package com.planup.user.ui.dto;

import com.planup.user.domain.SocialType;
import com.planup.user.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String socialType; // ✅ 핵심: "KAKAO", "NAVER", "LOCAL" 문자열 반환

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.socialType = user.getSocialType().name(); // Enum -> String 변환
    }
}