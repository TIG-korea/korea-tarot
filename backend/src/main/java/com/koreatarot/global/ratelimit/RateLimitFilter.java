package com.koreatarot.global.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.error.ProblemDetailResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final RateLimitConfig rateLimitConfig;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitConfig rateLimitConfig,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.rateLimitConfig = rateLimitConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RateLimitConfig.Policy policy = rateLimitConfig.resolve(request).orElse(null);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isLimited(request, policy)) {
            writeRateLimitResponse(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLimited(HttpServletRequest request, RateLimitConfig.Policy policy) {
        try {
            String key = "rate:" + policy.name() + ":" + identifier(request);
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, policy.window());
            }
            return count != null && count > policy.limit();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String identifier(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            return "auth:" + sha256(authorization);
        }
        return "ip:" + clientIp(request);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void writeRateLimitResponse(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(ErrorCode.RATE_LIMITED.status().value());
        response.setContentType("application/problem+json;charset=UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ProblemDetailResponse.of(
                        ErrorCode.RATE_LIMITED,
                        "요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요.",
                        request.getRequestURI()
                )
        );
    }
}
