package com.koreatarot.auth;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
