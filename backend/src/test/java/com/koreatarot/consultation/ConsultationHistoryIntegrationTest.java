package com.koreatarot.consultation;

import com.koreatarot.consultation.dto.ConsultationCursor;
import com.koreatarot.consultation.dto.ConsultationHistoryDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.entity.ConsultationCard;
import com.koreatarot.consultation.enums.SpreadType;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.consultation.service.ConsultationHistoryService;
import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationHistoryIntegrationTest {

    private final ConsultationRepository consultationRepository = mock(ConsultationRepository.class);
    private final ConsultationCardRepository consultationCardRepository = mock(ConsultationCardRepository.class);
    private final TarotCardRepository tarotCardRepository = mock(TarotCardRepository.class);
    private final ConsultationHistoryService consultationHistoryService = new ConsultationHistoryService(
            consultationRepository,
            consultationCardRepository,
            tarotCardRepository
    );

    @Test
    void listReturnsLatestHistoryWithCursorAndCardNames() {
        Consultation first = completedConsultation(1002L);
        Consultation second = completedConsultation(1001L);

        when(consultationRepository.findHistory(eq(1L), isNull(), any(Pageable.class)))
                .thenReturn(List.of(first, second));
        when(consultationCardRepository.findByConsultationIdOrderByPositionOrderAsc(1002L))
                .thenReturn(consultationCards(1002L));
        when(tarotCardRepository.findAllById(List.of(6L, 18L, 14L))).thenReturn(tarotCards());

        ConsultationHistoryDto.ListResponse response = consultationHistoryService.list(1L, null, 1);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).consultationId()).isEqualTo(1002L);
        assertThat(response.items().get(0).cardNames()).containsExactly("The Lovers", "The Moon", "Temperance");
        assertThat(response.items().get(0).summary()).isEqualTo("관계에 대한 중요한 선택의 시기입니다.");
        assertThat(ConsultationCursor.from(response.nextCursor()).id()).isEqualTo(1002L);
    }

    @Test
    void getDetailReturnsStoredResultAndSelectedCards() {
        Consultation consultation = completedConsultation(1001L);

        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 1L))
                .thenReturn(Optional.of(consultation));
        when(consultationCardRepository.findByConsultationIdOrderByPositionOrderAsc(1001L))
                .thenReturn(consultationCards(1001L));
        when(tarotCardRepository.findAllById(List.of(6L, 18L, 14L))).thenReturn(tarotCards());

        ConsultationHistoryDto.DetailResponse response = consultationHistoryService.getDetail(1L, 1001L);

        assertThat(response.consultationId()).isEqualTo(1001L);
        assertThat(response.concern()).isEqualTo("현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.");
        assertThat(response.result().get("summary").asText()).isEqualTo("관계에 대한 중요한 선택의 시기입니다.");
        assertThat(response.cards())
                .extracting(ConsultationHistoryDto.Card::positionName)
                .containsExactly("현재 상황", "장애물 또는 숨겨진 원인", "조언 또는 방향");
    }

    @Test
    void deleteSoftDeletesOwnedConsultation() {
        Consultation consultation = completedConsultation(1001L);

        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 1L))
                .thenReturn(Optional.of(consultation));

        consultationHistoryService.delete(1L, 1001L);

        assertThat(consultation.getDeletedAt()).isNotNull();
        verify(consultationRepository).save(consultation);
    }

    private Consultation completedConsultation(Long id) {
        Consultation consultation = Consultation.builder()
                .userId(1L)
                .concern("현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.")
                .spreadType(SpreadType.THREE_CARD)
                .idempotencyKey("key-" + id)
                .build();
        ReflectionTestUtils.setField(consultation, "id", id);
        consultation.complete(
                "love",
                "관계에 대한 중요한 선택의 시기입니다.",
                """
                        {
                          "summary": "관계에 대한 중요한 선택의 시기입니다.",
                          "overall": "전체 흐름입니다.",
                          "cards": [],
                          "advice": "현실적인 조언입니다.",
                          "caution": "주의할 점입니다."
                        }
                        """,
                "[\"card-lovers-present-v1\"]",
                "mock-tarot-v1",
                "mock",
                "tarot-v1.0",
                "tarot-doc-v1.0"
        );
        return consultation;
    }

    private List<ConsultationCard> consultationCards(Long consultationId) {
        return List.of(
                consultationCard(consultationId, 6L, 1, PositionCode.PRESENT),
                consultationCard(consultationId, 18L, 2, PositionCode.OBSTACLE),
                consultationCard(consultationId, 14L, 3, PositionCode.ADVICE)
        );
    }

    private ConsultationCard consultationCard(
            Long consultationId,
            Long cardId,
            Integer positionOrder,
            PositionCode positionCode
    ) {
        return ConsultationCard.builder()
                .consultationId(consultationId)
                .cardId(cardId)
                .positionOrder(positionOrder)
                .positionCode(positionCode)
                .orientation(CardOrientation.UPRIGHT)
                .build();
    }

    private List<TarotCard> tarotCards() {
        return List.of(
                tarotCard(6L, "The Lovers", "연인", 6),
                tarotCard(18L, "The Moon", "달", 18),
                tarotCard(14L, "Temperance", "절제", 14)
        );
    }

    private TarotCard tarotCard(Long id, String nameEn, String nameKo, Integer cardNumber) {
        TarotCard tarotCard = TarotCard.builder()
                .nameEn(nameEn)
                .nameKo(nameKo)
                .arcana(Arcana.MAJOR)
                .cardNumber(cardNumber)
                .imageUrl("/images/cards/" + cardNumber + ".png")
                .build();
        ReflectionTestUtils.setField(tarotCard, "id", id);
        return tarotCard;
    }
}
