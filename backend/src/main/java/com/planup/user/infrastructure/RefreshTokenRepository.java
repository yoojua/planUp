package com.planup.user.infrastructure;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;

@RedisHash
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
