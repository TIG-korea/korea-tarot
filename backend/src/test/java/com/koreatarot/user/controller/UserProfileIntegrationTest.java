package com.koreatarot.user.controller;

import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.security.AuthenticatedUser;
import com.koreatarot.user.dto.UserDto;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.enums.UserStatus;
import com.koreatarot.user.repository.UserRepository;
import com.koreatarot.user.service.UserWithdrawalService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserProfileIntegrationTest {

    @Test
    void meReturnsCurrentUserProfile() {
        UserRepository userRepository = mock(UserRepository.class);
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserController userController = new UserController(userRepository, mock(UserWithdrawalService.class));
        ApiResponse<UserDto.ProfileResponse> response =
                userController.me(new AuthenticatedUser(1L, "user@example.com"));

        assertThat(response.success()).isTrue();
        assertThat(response.data().id()).isEqualTo(1L);
        assertThat(response.data().email()).isEqualTo("user@example.com");
        assertThat(response.data().nickname()).isEqualTo("tarouser");
        assertThat(response.data().status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void meRejectsUnauthenticatedRequest() {
        UserController userController = new UserController(mock(UserRepository.class), mock(UserWithdrawalService.class));

        assertThatThrownBy(() -> userController.me(null))
                .isInstanceOf(BusinessException.class);
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
