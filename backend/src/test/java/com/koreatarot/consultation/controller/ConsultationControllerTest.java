package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.enums.SpreadType;
import com.koreatarot.consultation.service.ConsultationService;
import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.security.AuthenticatedUser;
import com.koreatarot.tarot.enums.PositionCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultationControllerTest {

    private final ConsultationService consultationService = mock(ConsultationService.class);
    private final ConsultationController controller = new ConsultationController(consultationService);

    @Test
    void createConsultationReturnsPendingStatusAndStreamUrl() {
        String idempotencyKey = "550e8400-e29b-41d4-a716-446655440000";
        ConsultationSelectionDto.CreateRequest request = request();
        Consultation consultation = Consultation.builder()
                .userId(1L)
                .concern("현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.")
                .spreadType(SpreadType.THREE_CARD)
                .idempotencyKey(idempotencyKey)
                .build();
        ReflectionTestUtils.setField(consultation, "id", 1001L);

        when(consultationService.create(1L, idempotencyKey, request)).thenReturn(consultation);

        ApiResponse<ConsultationSelectionDto.CreateResponse> response = controller.createConsultation(
                new AuthenticatedUser(1L, "user@example.com"),
                idempotencyKey,
                request
        );

        assertThat(response.success()).isTrue();
        assertThat(response.data().consultationId()).isEqualTo(1001L);
        assertThat(response.data().status()).isEqualTo("PENDING");
        assertThat(response.data().streamUrl()).isEqualTo("/api/v1/consultations/1001/events");
    }

    @Test
    void createConsultationRejectsUnauthenticatedRequest() {
        assertThatThrownBy(() -> controller.createConsultation(
                null,
                "550e8400-e29b-41d4-a716-446655440000",
                request()
        )).isInstanceOf(BusinessException.class);
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
}
