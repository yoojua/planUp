package com.planup.repository;

import com.planup.domain.token.RefreshToken;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;

@RedisHash
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
