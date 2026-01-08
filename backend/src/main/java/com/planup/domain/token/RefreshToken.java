package com.planup.domain.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14) // 14일
public class RefreshToken {

    @Id
    private String refreshToken; // 토큰 자체가 Key가 된다.

    private String email; // 유저 식별자

    // TODO: 권한에 따른 추가 기능
}
