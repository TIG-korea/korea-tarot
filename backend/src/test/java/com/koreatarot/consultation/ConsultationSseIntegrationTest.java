package com.koreatarot.consultation;

import com.koreatarot.ai.client.AiInterpretationClient;
import com.koreatarot.ai.dto.AiInterpretationDto;
import com.koreatarot.ai.service.AiRequestLogService;
import com.koreatarot.consultation.dto.ConsultationEventDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.entity.ConsultationCard;
import com.koreatarot.consultation.enums.ConsultationStatus;
import com.koreatarot.consultation.enums.SpreadType;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.consultation.service.ConsultationEventService;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultationSseIntegrationTest {

    private final ConsultationRepository consultationRepository = mock(ConsultationRepository.class);
    private final ConsultationCardRepository consultationCardRepository = mock(ConsultationCardRepository.class);
    private final TarotCardRepository tarotCardRepository = mock(TarotCardRepository.class);
    private final AiInterpretationClient aiInterpretationClient = mock(AiInterpretationClient.class);
    private final AiRequestLogService aiRequestLogService = mock(AiRequestLogService.class);
    private final ConsultationEventService consultationEventService = new ConsultationEventService(
            consultationRepository,
            consultationCardRepository,
            tarotCardRepository,
            aiInterpretationClient,
            aiRequestLogService
    );

    @Test
    void streamSendsMetaBeforeAiTokenEvents() {
        Consultation consultation = consultation(1L, 1001L);
        List<ConsultationCard> consultationCards = consultationCards(1001L);
        List<TarotCard> tarotCards = tarotCards();

        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 1L))
                .thenReturn(Optional.of(consultation));
        when(consultationCardRepository.findByConsultationIdOrderByPositionOrderAsc(1001L))
                .thenReturn(consultationCards);
        when(tarotCardRepository.findAllById(List.of(6L, 18L, 14L))).thenReturn(tarotCards);
        when(aiInterpretationClient.streamInterpretation(any()))
                .thenReturn(Flux.just(ServerSentEvent.builder("{\"text\":\"이번 리딩은\"}").event("token").build()));

        List<ServerSentEvent<Object>> events = consultationEventService.stream(1L, 1001L)
                .collectList()
                .block();

        assertThat(events).hasSize(2);
        assertThat(events.get(0).event()).isEqualTo("meta");
        assertThat(events.get(1).event()).isEqualTo("token");
        assertThat(events.get(1).data()).isEqualTo("{\"text\":\"이번 리딩은\"}");
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.STREAMING);

        ConsultationEventDto.MetaEvent metaEvent = (ConsultationEventDto.MetaEvent) events.get(0).data();
        assertThat(metaEvent.consultationId()).isEqualTo(1001L);
        assertThat(metaEvent.cards())
                .extracting(ConsultationEventDto.CardMeta::positionCode)
                .containsExactly("PRESENT", "OBSTACLE", "ADVICE");
        assertThat(metaEvent.cards())
                .extracting(ConsultationEventDto.CardMeta::cardName)
                .containsExactly("The Lovers", "The Moon", "Temperance");

        ArgumentCaptor<AiInterpretationDto.StreamRequest> captor =
                ArgumentCaptor.forClass(AiInterpretationDto.StreamRequest.class);
        org.mockito.Mockito.verify(aiInterpretationClient).streamInterpretation(captor.capture());
        assertThat(captor.getValue().requestId()).startsWith("req-");
        assertThat(captor.getValue().consultationId()).isEqualTo(1001L);
        assertThat(captor.getValue().locale()).isEqualTo("ko");
        assertThat(captor.getValue().cards()).hasSize(3);
    }

    @Test
    void streamReturnsErrorEventWhenAiStreamFails() {
        Consultation consultation = consultation(1L, 1001L);

        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 1L))
                .thenReturn(Optional.of(consultation));
        when(consultationCardRepository.findByConsultationIdOrderByPositionOrderAsc(1001L))
                .thenReturn(consultationCards(1001L));
        when(tarotCardRepository.findAllById(List.of(6L, 18L, 14L))).thenReturn(tarotCards());
        when(aiInterpretationClient.streamInterpretation(any()))
                .thenReturn(Flux.error(new RuntimeException("AI timeout")));

        List<ServerSentEvent<Object>> events = consultationEventService.stream(1L, 1001L)
                .collectList()
                .block();

        assertThat(events).hasSize(2);
        assertThat(events.get(0).event()).isEqualTo("meta");
        assertThat(events.get(1).event()).isEqualTo("error");
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.FAILED);

        ConsultationEventDto.ErrorEvent errorEvent = (ConsultationEventDto.ErrorEvent) events.get(1).data();
        assertThat(errorEvent.code()).isEqualTo("AI_GENERATION_FAILED");
    }

    @Test
    void streamSavesResultWhenDoneEventArrives() {
        Consultation consultation = consultation(1L, 1001L);

        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 1L))
                .thenReturn(Optional.of(consultation));
        when(consultationCardRepository.findByConsultationIdOrderByPositionOrderAsc(1001L))
                .thenReturn(consultationCards(1001L));
        when(tarotCardRepository.findAllById(List.of(6L, 18L, 14L))).thenReturn(tarotCards());
        when(aiInterpretationClient.streamInterpretation(any()))
                .thenReturn(Flux.just(ServerSentEvent.builder(donePayload()).event("done").build()));

        List<ServerSentEvent<Object>> events = consultationEventService.stream(1L, 1001L)
                .collectList()
                .block();

        assertThat(events).hasSize(2);
        assertThat(events.get(1).event()).isEqualTo("done");
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.COMPLETED);
        assertThat(consultation.getCategoryCode()).isEqualTo("love");
        assertThat(consultation.getResultSummary()).isEqualTo("관계에 대한 중요한 선택의 시기입니다.");
        assertThat(consultation.getResultDetail()).contains("\"summary\"");
        assertThat(consultation.getRetrievedDocIds()).contains("card-lovers-present-v1");
        assertThat(consultation.getCompletedAt()).isNotNull();
        org.mockito.Mockito.verify(consultationRepository).save(consultation);
        org.mockito.Mockito.verify(aiRequestLogService).logSuccess(
                1001L,
                "req-abc-123",
                0,
                0,
                100
        );
    }

    @Test
    void streamRejectsNonOwnerConsultation() {
        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationEventService.stream(2L, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상담을 찾을 수 없습니다.");
    }

    private Consultation consultation(Long userId, Long consultationId) {
        Consultation consultation = Consultation.builder()
                .userId(userId)
                .concern("현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.")
                .spreadType(SpreadType.THREE_CARD)
                .idempotencyKey("550e8400-e29b-41d4-a716-446655440000")
                .build();
        ReflectionTestUtils.setField(consultation, "id", consultationId);
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

    private String donePayload() {
        return """
                {
                  "requestId": "req-abc-123",
                  "status": "success",
                  "category": "love",
                  "modelName": "mock-tarot-v1",
                  "modelProvider": "mock",
                  "promptVersion": "tarot-v1.0",
                  "documentVersion": "tarot-doc-v1.0",
                  "retrievedDocIds": ["card-lovers-present-v1"],
                  "result": {
                    "summary": "관계에 대한 중요한 선택의 시기입니다.",
                    "overall": "전체 흐름 해석입니다.",
                    "cards": [],
                    "advice": "현실적인 조언입니다.",
                    "caution": "주의할 점입니다."
                  },
                  "usage": {
                    "inputTokens": 0,
                    "outputTokens": 0,
                    "latencyMs": 100
                  }
                }
                """;
    }
}
