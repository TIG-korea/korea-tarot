package com.koreatarot.global.security;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public RefreshTokenService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key(token), String.valueOf(userId), ttl());
        return token;
    }

    public Long rotate(String refreshToken) {
        Long userId = findUserId(refreshToken);
        delete(refreshToken);
        return userId;
    }

    public void delete(String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.delete(key(refreshToken));
        }
    }

    private Long findUserId(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 refresh token입니다.");
        }

        String userId = redisTemplate.opsForValue().get(key(refreshToken));
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 refresh token입니다.");
        }
        return Long.valueOf(userId);
    }

    private Duration ttl() {
        return Duration.ofDays(authProperties.refreshTokenTtlDays());
    }

    private String key(String refreshToken) {
        return KEY_PREFIX + refreshToken;
    }
}
