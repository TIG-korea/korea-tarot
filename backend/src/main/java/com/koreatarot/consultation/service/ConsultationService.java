package com.koreatarot.consultation.service;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.entity.ConsultationCard;
import com.koreatarot.consultation.enums.SpreadType;
import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.enums.UserStatus;
import com.koreatarot.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ConsultationCardRepository consultationCardRepository;
    private final DraftDeckService draftDeckService;
    private final CardSelectionValidator cardSelectionValidator;
    private final IdempotencyService idempotencyService;
    private final UserRepository userRepository;

    public ConsultationService(
            ConsultationRepository consultationRepository,
            ConsultationCardRepository consultationCardRepository,
            DraftDeckService draftDeckService,
            CardSelectionValidator cardSelectionValidator,
            IdempotencyService idempotencyService,
            UserRepository userRepository
    ) {
        this.consultationRepository = consultationRepository;
        this.consultationCardRepository = consultationCardRepository;
        this.draftDeckService = draftDeckService;
        this.cardSelectionValidator = cardSelectionValidator;
        this.idempotencyService = idempotencyService;
        this.userRepository = userRepository;
    }

    @Transactional
    public Consultation create(
            Long userId,
            String idempotencyKey,
            ConsultationSelectionDto.CreateRequest request
    ) {
        validateActiveUser(userId);
        String key = idempotencyService.validate(idempotencyKey);
        return idempotencyService.findExisting(userId, key)
                .orElseGet(() -> createNew(userId, key, request));
    }

    private void validateActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "탈퇴 요청된 사용자는 새 상담을 만들 수 없습니다.");
        }
    }

    private Consultation createNew(
            Long userId,
            String idempotencyKey,
            ConsultationSelectionDto.CreateRequest request
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "요청 본문은 필수입니다.");
        }

        cardSelectionValidator.validate(request.selections());
        DraftDeckService.DraftDeck draftDeck = draftDeckService.consume(request.draftId(), userId);

        Consultation consultation = consultationRepository.save(Consultation.builder()
                .userId(userId)
                .concern(draftDeck.concern())
                .spreadType(SpreadType.THREE_CARD)
                .idempotencyKey(idempotencyKey)
                .build());

        List<ConsultationCard> cards = request.selections().stream()
                .sorted(Comparator.comparing(selection -> positionOrder(selection.positionCode())))
                .map(selection -> toConsultationCard(consultation.getId(), draftDeck, selection))
                .toList();
        consultationCardRepository.saveAll(cards);

        return consultation;
    }

    private ConsultationCard toConsultationCard(
            Long consultationId,
            DraftDeckService.DraftDeck draftDeck,
            ConsultationSelectionDto.CardSelectionRequest selection
    ) {
        return ConsultationCard.builder()
                .consultationId(consultationId)
                .cardId(draftDeck.deckMapping().get(selection.deckIndex()))
                .positionOrder(positionOrder(selection.positionCode()))
                .positionCode(selection.positionCode())
                .orientation(CardOrientation.UPRIGHT)
                .build();
    }

    private int positionOrder(PositionCode positionCode) {
        return switch (positionCode) {
            case PRESENT -> 1;
            case OBSTACLE -> 2;
            case ADVICE -> 3;
        };
    }
}
