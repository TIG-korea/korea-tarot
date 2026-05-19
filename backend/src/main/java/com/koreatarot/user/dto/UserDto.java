package com.koreatarot.user.dto;

import com.koreatarot.user.entity.User;
import com.koreatarot.user.enums.UserStatus;

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
