package com.koreatarot.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public PasswordService(PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    public String hash(String rawPassword) {
        passwordValidator.validate(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
