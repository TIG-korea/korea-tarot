package com.koreatarot.auth;

import com.koreatarot.user.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {

    public record SignupRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Size(min = 2, max = 20) String nickname,
            @AssertTrue boolean termsAgreed,
            @AssertTrue boolean privacyAgreed,
            boolean marketingAgreed
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record SignupResponse(
            Long userId,
            String email,
            String nickname,
            String accessToken
    ) {
        public static SignupResponse of(User user, String accessToken) {
            return new SignupResponse(user.getId(), user.getEmail(), user.getNickname(), accessToken);
        }
    }

    public record LoginResponse(
            String accessToken,
            UserSummary user
    ) {
        public static LoginResponse of(User user, String accessToken) {
            return new LoginResponse(accessToken, UserSummary.from(user));
        }
    }

    public record RefreshResponse(
            String accessToken
    ) {
    }

    public record UserSummary(
            Long id,
            String email,
            String nickname
    ) {
        public static UserSummary from(User user) {
            return new UserSummary(user.getId(), user.getEmail(), user.getNickname());
        }
    }

    public record TokenPair(
            String accessToken,
            String refreshToken
    ) {
    }

    private AuthDto() {
    }
}
