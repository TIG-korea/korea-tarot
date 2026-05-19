package com.koreatarot.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS_TOKEN = "access";
    private static final String USER_ID = "userId";
    private static final String EMAIL = "email";

    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    public JwtTokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(authProperties.accessTokenTtlMinutes()));

        return Jwts.builder()
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .claim(USER_ID, userId)
                .claim(EMAIL, email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get(TOKEN_TYPE, String.class);
        if (!ACCESS_TOKEN.equals(tokenType)) {
            throw new IllegalArgumentException("유효하지 않은 토큰 유형입니다.");
        }

        Long userId = claims.get(USER_ID, Long.class);
        String email = claims.get(EMAIL, String.class);
        return new AuthenticatedUser(userId, email);
    }
}
