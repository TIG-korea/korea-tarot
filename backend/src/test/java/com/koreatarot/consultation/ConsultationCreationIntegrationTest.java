package com.koreatarot.consultation;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.enums.ConsultationStatus;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.consultation.service.CardSelectionValidator;
import com.koreatarot.consultation.service.ConsultationService;
import com.koreatarot.consultation.service.DraftDeckService;
import com.koreatarot.consultation.service.IdempotencyService;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationCreationIntegrationTest {

    private final ConsultationRepository consultationRepository = mock(ConsultationRepository.class);
    private final ConsultationCardRepository consultationCardRepository = mock(ConsultationCardRepository.class);
    private final DraftDeckService draftDeckService = mock(DraftDeckService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CardSelectionValidator cardSelectionValidator = new CardSelectionValidator();
    private final IdempotencyService idempotencyService = new IdempotencyService(consultationRepository);
    private final ConsultationService consultationService = new ConsultationService(
            consultationRepository,
            consultationCardRepository,
            draftDeckService,
            cardSelectionValidator,
            idempotencyService,
            userRepository
    );

    @Test
    void createConsultationSavesPendingConsultationAndThreeCards() {
        Long userId = 1L;
        String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        ConsultationSelectionDto.CreateRequest request = request();
        DraftDeckService.DraftDeck draftDeck = new DraftDeckService.DraftDeck(
                "drf_abc123",
                userId,
                "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
                List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L),
                Instant.parse("2026-05-19T10:40:00Z"),
                false
        );

        when(consultationRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(draftDeckService.consume("drf_abc123", userId)).thenReturn(draftDeck);
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(invocation -> {
            Consultation consultation = invocation.getArgument(0);
            ReflectionTestUtils.setField(consultation, "id", 1001L);
            return consultation;
        });

        Consultation result = consultationService.create(userId, idempotencyKey, request);

        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(ConsultationStatus.PENDING);
        assertThat(result.getConcern()).isEqualTo(draftDeck.concern());
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);

        verify(draftDeckService).consume("drf_abc123", userId);
        verify(consultationCardRepository).saveAll(any());
    }

    @Test
    void createConsultationReturnsExistingConsultationForSameIdempotencyKey() {
        Long userId = 1L;
        String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        Consultation existing = Consultation.builder()
                .userId(userId)
                .concern("이미 생성된 상담입니다.")
                .spreadType(com.koreatarot.consultation.enums.SpreadType.THREE_CARD)
                .idempotencyKey(idempotencyKey)
                .build();
        ReflectionTestUtils.setField(existing, "id", 1001L);

        when(consultationRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey))
                .thenReturn(Optional.of(existing));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId)));

        Consultation result = consultationService.create(userId, idempotencyKey, request());

        assertThat(result.getId()).isEqualTo(1001L);
        verify(draftDeckService, never()).consume(any(), any());
        verify(consultationRepository, never()).save(any());
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
