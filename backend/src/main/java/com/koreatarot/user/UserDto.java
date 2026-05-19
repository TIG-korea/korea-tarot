package com.koreatarot.user;

import java.time.LocalDateTime;

public final class UserDto {

    public record ProfileResponse(
            Long id,
            String email,
            String nickname,
            UserStatus status,
            LocalDateTime createdAt
    ) {
        public static ProfileResponse from(User user) {
            return new ProfileResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getStatus(),
                    user.getCreatedAt()
            );
        }
    }

    private UserDto() {
    }
}
