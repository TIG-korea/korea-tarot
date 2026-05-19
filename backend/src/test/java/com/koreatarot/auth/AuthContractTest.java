package com.koreatarot.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.GlobalExceptionHandler;
import com.koreatarot.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        AuthProperties authProperties = new AuthProperties(
                "change-this-local-development-jwt-secret-at-least-32-bytes",
                15,
                7
        );
        AuthController authController = new AuthController(authService, authProperties);
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void signupReturnsAccessTokenAndRefreshCookie() throws Exception {
        User user = user();
        when(authService.signup(any(AuthDto.SignupRequest.class)))
                .thenReturn(new AuthService.AuthResult(user, "access-token", "refresh-token"));

        AuthDto.SignupRequest request = new AuthDto.SignupRequest(
                "user@example.com",
                "Password123!",
                "tarouser",
                true,
                true,
                false
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tarouser"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void loginReturnsAccessTokenAndUserSummary() throws Exception {
        User user = user();
        when(authService.login(any(AuthDto.LoginRequest.class)))
                .thenReturn(new AuthService.AuthResult(user, "access-token", "refresh-token"));

        AuthDto.LoginRequest request = new AuthDto.LoginRequest("user@example.com", "Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.user.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.user.nickname").value("tarouser"));
    }

    @Test
    void refreshRotatesRefreshCookie() throws Exception {
        when(authService.refresh("old-refresh-token"))
                .thenReturn(new AuthDto.TokenPair("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", "new-refresh-token"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void logoutDeletesRefreshTokenAndExpiresCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout("refresh-token");
    }

    private User user() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("password-hash")
                .nickname("tarouser")
                .termsAgreedAt(now)
                .privacyAgreedAt(now)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
