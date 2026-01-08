package com.planup.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 회원 찾기 (로그인 시 사용)
    Optional<User> findByEmail(String email);

    // 닉네임 중복 검사 등에 사용
    boolean existsByNickname(String nickname);

    // 소셜 타입과 식별값으로 회원 찾기 (OAuth 로그인 시 사용)
    Optional<User> findBySocialTypeAndSocialId(String socialType, String socialId);
}
