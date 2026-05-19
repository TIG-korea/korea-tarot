package com.koreatarot.user.controller;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.consultation.service.CardSelectionValidator;
import com.koreatarot.consultation.service.ConsultationService;
import com.koreatarot.consultation.service.DraftDeckService;
import com.koreatarot.consultation.service.IdempotencyService;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.security.AuthenticatedUser;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.enums.UserStatus;
import com.koreatarot.user.repository.UserRepository;
import com.koreatarot.user.service.UserWithdrawalService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserWithdrawalIntegrationTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserWithdrawalService userWithdrawalService = new UserWithdrawalService(userRepository);

    @Test
    void withdrawMarksUserAsWithdrawalRequested() {
        User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserController userController = new UserController(userRepository, userWithdrawalService);

        userController.withdraw(new AuthenticatedUser(1L, "user@example.com"));

        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWAL_REQUESTED);
        verify(userRepository).save(user);
    }

    @Test
    void withdrawnUserCannotCreateConsultation() {
        User user = user(1L);
        user.requestWithdrawal();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ConsultationService consultationService = new ConsultationService(
                mock(ConsultationRepository.class),
                mock(ConsultationCardRepository.class),
                mock(DraftDeckService.class),
                new CardSelectionValidator(),
                new IdempotencyService(mock(ConsultationRepository.class)),
                userRepository
        );

        assertThatThrownBy(() -> consultationService.create(
                1L,
                "550e8400-e29b-41d4-a716-446655440000",
                request()
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("탈퇴 요청된 사용자는 새 상담을 만들 수 없습니다.");
    }

    private ConsultationSelectionDto.CreateRequest request() {
        return new ConsultationSelectionDto.CreateRequest(
                "drf_abc123",
                List.of(
                        new ConsultationSelectionDto.CardSelectionRequest(4, PositionCode.PRESENT),
                        new ConsultationSelectionDto.CardSelectionRequest(11, PositionCode.OBSTACLE),
                        new ConsultationSelectionDto.CardSelectionRequest(17, PositionCode.ADVICE)
                )
        );
    }

    private User user(Long id) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 19, 10, 0);
        User user = User.builder()
                .email("user@example.com")
                .passwordHash("password-hash")
                .nickname("tarouser")
                .termsAgreedAt(now)
                .privacyAgreedAt(now)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
