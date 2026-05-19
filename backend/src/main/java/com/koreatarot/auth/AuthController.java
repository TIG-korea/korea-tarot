package com.koreatarot.auth;

import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.openapi.docs.AuthApiDocs;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@AuthApiDocs.AuthTag
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final AuthProperties authProperties;

    public AuthController(AuthService authService, AuthProperties authProperties) {
        this.authService = authService;
        this.authProperties = authProperties;
    }

    @PostMapping("/signup")
    @AuthApiDocs.Signup
    public ApiResponse<AuthDto.SignupResponse> signup(
            @Valid @RequestBody AuthDto.SignupRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.signup(request);
        addRefreshCookie(response, result.refreshToken());
        return ApiResponse.success(AuthDto.SignupResponse.of(result.user(), result.accessToken()));
    }

    @PostMapping("/login")
    @AuthApiDocs.Login
    public ApiResponse<AuthDto.LoginResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.login(request);
        addRefreshCookie(response, result.refreshToken());
        return ApiResponse.success(AuthDto.LoginResponse.of(result.user(), result.accessToken()));
    }

    @PostMapping("/refresh")
    @AuthApiDocs.Refresh
    public ApiResponse<AuthDto.RefreshResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthDto.TokenPair tokenPair = authService.refresh(refreshToken);
        addRefreshCookie(response, tokenPair.refreshToken());
        return ApiResponse.success(new AuthDto.RefreshResponse(tokenPair.accessToken()));
    }

    @PostMapping("/logout")
    @AuthApiDocs.Logout
    public ApiResponse<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        expireRefreshCookie(response);
        return ApiResponse.empty();
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(authProperties.refreshTokenTtlDays()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void expireRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
