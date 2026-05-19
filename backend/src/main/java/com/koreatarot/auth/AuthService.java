package com.koreatarot.auth;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.user.User;
import com.koreatarot.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResult signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 가입된 이메일입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordService.hash(request.password()))
                .nickname(request.nickname())
                .termsAgreedAt(now)
                .privacyAgreedAt(now)
                .marketingAgreedAt(request.marketingAgreed() ? now : null)
                .build();
        User savedUser = userRepository.save(user);

        return issueTokens(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResult login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(this::invalidCredentials);

        if (!passwordService.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthDto.TokenPair refresh(String refreshToken) {
        Long userId = refreshTokenService.rotate(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 refresh token입니다."));

        String newAccessToken = jwtTokenService.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = refreshTokenService.create(user.getId());
        return new AuthDto.TokenPair(newAccessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenService.delete(refreshToken);
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.create(user.getId());
        return new AuthResult(user, accessToken, refreshToken);
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    public record AuthResult(
            User user,
            String accessToken,
            String refreshToken
    ) {
    }
}
