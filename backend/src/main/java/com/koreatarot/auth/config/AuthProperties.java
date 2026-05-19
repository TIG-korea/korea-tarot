package com.koreatarot.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays
) {
}
