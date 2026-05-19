package com.koreatarot.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RateLimitConfig {

    public Optional<Policy> resolve(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (HttpMethod.POST.matches(method) && "/api/v1/auth/signup".equals(path)) {
            return Optional.of(new Policy("signup", 5, Duration.ofDays(1)));
        }
        if (HttpMethod.POST.matches(method) && "/api/v1/auth/login".equals(path)) {
            return Optional.of(new Policy("login", 5, Duration.ofMinutes(1)));
        }
        if (HttpMethod.POST.matches(method) && "/api/v1/consultations".equals(path)) {
            return Optional.of(new Policy("consultation-create", 20, Duration.ofDays(1)));
        }
        return Optional.empty();
    }

    public record Policy(
            String name,
            long limit,
            Duration window
    ) {
    }
}
