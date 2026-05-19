package com.koreatarot.auth.security;

public record AuthenticatedUser(
        Long id,
        String email
) {
}
