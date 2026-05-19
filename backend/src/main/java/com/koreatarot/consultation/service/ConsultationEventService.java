package com.koreatarot.consultation.service;

import com.koreatarot.ai.client.AiInterpretationClient;
import com.koreatarot.ai.dto.AiInterpretationDto;
import com.koreatarot.ai.service.AiRequestLogService;
import com.koreatarot.consultation.dto.ConsultationEventDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.entity.ConsultationCard;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.repository.TarotCardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultationEventService {

    private static final String LOCALE_KO = "ko";
    private static final String AI_GENERATION_FAILED = "AI_GENERATION_FAILED";
    private static final String DONE_EVENT = "done";

    private final ConsultationRepository consultationRepository;
    private final ConsultationCardRepository consultationCardRepository;
    private final TarotCardRepository tarotCardRepository;
    private final AiInterpretationClient aiInterpretationClient;
    private final AiRequestLogService aiRequestLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConsultationEventService(
            ConsultationRepository consultationRepository,
            ConsultationCardRepository consultationCardRepository,
            TarotCardRepository tarotCardRepository,
            AiInterpretationClient aiInterpretationClient,
            AiRequestLogService aiRequestLogService
    ) {
        this.consultationRepository = consultationRepository;
        this.consultationCardRepository = consultationCardRepository;
        this.tarotCardRepository = tarotCardRepository;
        this.aiInterpretationClient = aiInterpretationClient;
        this.aiRequestLogService = aiRequestLogService;
    }

    @Transactional
    public Flux<ServerSentEvent<Object>> stream(Long userId, Long consultationId) {
        Consultation consultation = consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(consultationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상담을 찾을 수 없습니다."));
        List<ConsultationCard> consultationCards = consultationCardRepository
                .findByConsultationIdOrderByPositionOrderAsc(consultationId);
        Map<Long, TarotCard> tarotCards = tarotCardRepository.findAllById(toCardIds(consultationCards))
                .stream()
                .collect(Collectors.toMap(TarotCard::getId, Function.identity()));

        validateCards(consultationCards, tarotCards);
        consultation.startStreaming();

        ConsultationEventDto.MetaEvent metaEvent = toMetaEvent(consultationId, consultationCards, tarotCards);
        AiInterpretationDto.StreamRequest aiRequest = toAiRequest(consultation, consultationCards, tarotCards);

        return Flux.concat(
                        Flux.just(ServerSentEvent.builder((Object) metaEvent).event("meta").build()),
                        aiEvents(aiRequest, consultation)
                )
                .onErrorResume(throwable -> handleAiFailure(consultation));
    }

    private Flux<ServerSentEvent<Object>> aiEvents(
            AiInterpretationDto.StreamRequest aiRequest,
            Consultation consultation
    ) {
        try {
            return aiInterpretationClient.streamInterpretation(aiRequest)
                    .map(event -> handleAiEvent(event, consultation));
        } catch (RuntimeException exception) {
            return Flux.error(exception);
        }
    }

    private ServerSentEvent<Object> handleAiEvent(
            ServerSentEvent<String> source,
            Consultation consultation
    ) {
        if (DONE_EVENT.equals(source.event()) && source.data() != null) {
            completeConsultation(consultation, source.data());
        }
        return copyEvent(source);
    }

    private List<Long> toCardIds(List<ConsultationCard> consultationCards) {
        return consultationCards.stream()
                .map(ConsultationCard::getCardId)
                .toList();
    }

    private void validateCards(List<ConsultationCard> consultationCards, Map<Long, TarotCard> tarotCards) {
        if (consultationCards.size() != 3 || tarotCards.size() != 3) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상담 카드 정보를 찾을 수 없습니다.");
        }
    }

    private ConsultationEventDto.MetaEvent toMetaEvent(
            Long consultationId,
            List<ConsultationCard> consultationCards,
            Map<Long, TarotCard> tarotCards
    ) {
        List<ConsultationEventDto.CardMeta> cards = consultationCards.stream()
                .map(card -> {
                    TarotCard tarotCard = tarotCards.get(card.getCardId());
                    return new ConsultationEventDto.CardMeta(
                            card.getCardId(),
                            tarotCard.getNameEn(),
                            card.getPositionCode().name()
                    );
                })
                .toList();

        return new ConsultationEventDto.MetaEvent(consultationId, cards);
    }

    private AiInterpretationDto.StreamRequest toAiRequest(
            Consultation consultation,
            List<ConsultationCard> consultationCards,
            Map<Long, TarotCard> tarotCards
    ) {
        List<AiInterpretationDto.SelectedCard> selectedCards = consultationCards.stream()
                .map(card -> {
                    TarotCard tarotCard = tarotCards.get(card.getCardId());
                    return new AiInterpretationDto.SelectedCard(
                            card.getCardId(),
                            tarotCard.getNameEn(),
                            card.getPositionCode().name(),
                            card.getOrientation().name()
                    );
                })
                .toList();

        return new AiInterpretationDto.StreamRequest(
                "req-" + UUID.randomUUID(),
                consultation.getId(),
                consultation.getUserId(),
                consultation.getConcern(),
                consultation.getSpreadType().name(),
                selectedCards,
                LOCALE_KO
        );
    }

    private ServerSentEvent<Object> copyEvent(ServerSentEvent<String> source) {
        ServerSentEvent.Builder<Object> builder = ServerSentEvent.builder((Object) source.data());
        if (source.id() != null) {
            builder.id(source.id());
        }
        if (source.event() != null) {
            builder.event(source.event());
        }
        if (source.retry() != null) {
            builder.retry(source.retry());
        }
        if (source.comment() != null) {
            builder.comment(source.comment());
        }
        return builder.build();
    }

    private void completeConsultation(Consultation consultation, String donePayload) {
        try {
            JsonNode root = objectMapper.readTree(donePayload);
            JsonNode result = root.path("result");
            if (result.isMissingNode() || result.isNull()) {
                throw new IllegalArgumentException("AI done payload result is missing");
            }

            consultation.complete(
                    textOrNull(root, "category"),
                    textOrNull(result, "summary"),
                    objectMapper.writeValueAsString(result),
                    objectMapper.writeValueAsString(root.path("retrievedDocIds")),
                    textOrNull(root, "modelName"),
                    textOrNull(root, "modelProvider"),
                    textOrNull(root, "promptVersion"),
                    textOrNull(root, "documentVersion")
            );
            consultationRepository.save(consultation);
            JsonNode usage = root.path("usage");
            aiRequestLogService.logSuccess(
                    consultation.getId(),
                    textOrNull(root, "requestId"),
                    intOrNull(usage, "inputTokens"),
                    intOrNull(usage, "outputTokens"),
                    intOrNull(usage, "latencyMs")
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 완료 이벤트 저장에 실패했습니다.");
        }
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private ServerSentEvent<Object> errorEvent() {
        ConsultationEventDto.ErrorEvent event = new ConsultationEventDto.ErrorEvent(
                AI_GENERATION_FAILED,
                "해석 생성에 실패했습니다. 잠시 후 다시 시도해주세요."
        );
        return ServerSentEvent.builder((Object) event)
                .event("error")
                .build();
    }

    private Flux<ServerSentEvent<Object>> handleAiFailure(Consultation consultation) {
        consultation.fail();
        consultationRepository.save(consultation);
        aiRequestLogService.logFailure(
                consultation.getId(),
                null,
                AI_GENERATION_FAILED,
                "해석 생성에 실패했습니다."
        );
        return Flux.just(errorEvent());
    }
}
