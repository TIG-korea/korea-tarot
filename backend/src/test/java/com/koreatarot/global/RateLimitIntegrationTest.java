package com.koreatarot.global;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.ratelimit.RateLimitConfig;
import com.koreatarot.global.ratelimit.RateLimitFilter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class RateLimitIntegrationTest {

    private final RateLimitConfig rateLimitConfig = new RateLimitConfig();
    private final StringRedisTemplate stringRedisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final RateLimitFilter rateLimitFilter = new RateLimitFilter(
            rateLimitConfig,
            stringRedisTemplate,
            new ObjectMapper()
    );

    @Test
    void rateLimitRejectsLoginWhenLimitExceeded() throws Exception {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any(String.class))).thenReturn(6L);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        rateLimitFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("요청 횟수 제한을 초과했습니다.");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void rateLimitAllowsRequestWithinLimitAndSetsWindow() throws Exception {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any(String.class))).thenReturn(1L);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/signup");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        rateLimitFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(stringRedisTemplate).expire(any(String.class), eq(Duration.ofDays(1)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void rateLimitIgnoresUnconfiguredPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tarot/cards");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        rateLimitFilter.doFilter(request, response, filterChain);

        verify(stringRedisTemplate, never()).opsForValue();
        verify(filterChain).doFilter(request, response);
    }
}
