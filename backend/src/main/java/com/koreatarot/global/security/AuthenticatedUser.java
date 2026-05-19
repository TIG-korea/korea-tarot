package com.koreatarot.global.security;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
