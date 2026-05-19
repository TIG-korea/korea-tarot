package com.koreatarot.consultation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.consultation.dto.ConsultationCursor;
import com.koreatarot.consultation.dto.ConsultationHistoryDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.entity.ConsultationCard;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultationHistoryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;
    private static final int PREVIEW_LIMIT = 30;

    private final ConsultationRepository consultationRepository;
    private final ConsultationCardRepository consultationCardRepository;
    private final TarotCardRepository tarotCardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConsultationHistoryService(
            ConsultationRepository consultationRepository,
            ConsultationCardRepository consultationCardRepository,
            TarotCardRepository tarotCardRepository
    ) {
        this.consultationRepository = consultationRepository;
        this.consultationCardRepository = consultationCardRepository;
        this.tarotCardRepository = tarotCardRepository;
    }

    @Transactional(readOnly = true)
    public ConsultationHistoryDto.ListResponse list(Long userId, String cursor, Integer size) {
        int pageSize = normalizeSize(size);
        List<Consultation> rows = consultationRepository.findHistory(
                userId,
                ConsultationCursor.from(cursor).id(),
                PageRequest.of(0, pageSize + 1)
        );
        boolean hasNext = rows.size() > pageSize;
        List<Consultation> items = hasNext ? rows.subList(0, pageSize) : rows;
        String nextCursor = hasNext ? ConsultationCursor.encode(items.get(items.size() - 1).getId()) : null;

        return new ConsultationHistoryDto.ListResponse(
                items.stream()
                        .map(this::toItem)
                        .toList(),
                nextCursor
        );
    }

    @Transactional(readOnly = true)
    public ConsultationHistoryDto.DetailResponse getDetail(Long userId, Long consultationId) {
        Consultation consultation = findOwnedConsultation(userId, consultationId);
        List<ConsultationCard> consultationCards = consultationCardRepository
                .findByConsultationIdOrderByPositionOrderAsc(consultationId);
        Map<Long, TarotCard> tarotCards = tarotCardMap(consultationCards);

        return new ConsultationHistoryDto.DetailResponse(
                consultation.getId(),
                consultation.getConcern(),
                consultation.getSpreadType().name(),
                consultation.getCategoryCode(),
                consultation.getStatus().name(),
                consultationCards.stream()
                        .map(card -> toDetailCard(card, tarotCards.get(card.getCardId())))
                        .toList(),
                parseResult(consultation.getResultDetail()),
                consultation.getCreatedAt()
        );
    }

    @Transactional
    public void delete(Long userId, Long consultationId) {
        Consultation consultation = findOwnedConsultation(userId, consultationId);
        consultation.softDelete();
        consultationRepository.save(consultation);
    }

    private ConsultationHistoryDto.Item toItem(Consultation consultation) {
        List<ConsultationCard> consultationCards = consultationCardRepository
                .findByConsultationIdOrderByPositionOrderAsc(consultation.getId());
        Map<Long, TarotCard> tarotCards = tarotCardMap(consultationCards);

        return new ConsultationHistoryDto.Item(
                consultation.getId(),
                preview(consultation.getConcern()),
                consultationCards.stream()
                        .map(card -> tarotCards.get(card.getCardId()).getNameEn())
                        .toList(),
                consultation.getCategoryCode(),
                consultation.getResultSummary(),
                consultation.getStatus().name(),
                consultation.getCreatedAt()
        );
    }

    private ConsultationHistoryDto.Card toDetailCard(
            ConsultationCard consultationCard,
            TarotCard tarotCard
    ) {
        return new ConsultationHistoryDto.Card(
                consultationCard.getCardId(),
                tarotCard.getNameEn(),
                tarotCard.getNameKo(),
                consultationCard.getPositionOrder(),
                consultationCard.getPositionCode().name(),
                positionName(consultationCard.getPositionCode()),
                consultationCard.getOrientation().name(),
                tarotCard.getImageUrl()
        );
    }

    private Consultation findOwnedConsultation(Long userId, Long consultationId) {
        return consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(consultationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상담을 찾을 수 없습니다."));
    }

    private Map<Long, TarotCard> tarotCardMap(List<ConsultationCard> consultationCards) {
        List<Long> cardIds = consultationCards.stream()
                .map(ConsultationCard::getCardId)
                .toList();
        return tarotCardRepository.findAllById(cardIds)
                .stream()
                .collect(Collectors.toMap(TarotCard::getId, Function.identity()));
    }

    private JsonNode parseResult(String resultDetail) {
        if (resultDetail == null || resultDetail.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(resultDetail);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "상담 결과를 읽을 수 없습니다.");
        }
    }

    private String preview(String concern) {
        if (concern.length() <= PREVIEW_LIMIT) {
            return concern;
        }
        return concern.substring(0, PREVIEW_LIMIT) + "...";
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size < 1) {
            return 1;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String positionName(PositionCode positionCode) {
        return switch (positionCode) {
            case PRESENT -> "현재 상황";
            case OBSTACLE -> "장애물 또는 숨겨진 원인";
            case ADVICE -> "조언 또는 방향";
        };
    }
}
