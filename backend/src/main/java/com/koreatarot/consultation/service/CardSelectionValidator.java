package com.koreatarot.consultation.service;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.enums.PositionCode;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CardSelectionValidator {

    private static final int REQUIRED_SELECTION_COUNT = 3;
    private static final int MIN_DECK_INDEX = 0;
    private static final int MAX_DECK_INDEX = 21;

    public void validate(List<ConsultationSelectionDto.CardSelectionRequest> selections) {
        if (selections == null || selections.size() != REQUIRED_SELECTION_COUNT) {
            throw invalid("카드는 정확히 3장을 선택해야 합니다.");
        }

        Set<Integer> deckIndexes = new HashSet<>();
        Set<PositionCode> positionCodes = EnumSet.noneOf(PositionCode.class);

        for (ConsultationSelectionDto.CardSelectionRequest selection : selections) {
            validateSelection(selection);

            if (!deckIndexes.add(selection.deckIndex())) {
                throw invalid("같은 카드를 중복 선택할 수 없습니다.");
            }

            if (!positionCodes.add(selection.positionCode())) {
                throw invalid("같은 위치에 여러 카드를 배정할 수 없습니다.");
            }
        }

        if (!positionCodes.containsAll(EnumSet.allOf(PositionCode.class))) {
            throw invalid("현재 상황, 장애물, 조언 위치를 모두 선택해야 합니다.");
        }
    }

    private void validateSelection(ConsultationSelectionDto.CardSelectionRequest selection) {
        if (selection == null) {
            throw invalid("선택 정보는 필수입니다.");
        }

        Integer deckIndex = selection.deckIndex();
        if (deckIndex == null || deckIndex < MIN_DECK_INDEX || deckIndex > MAX_DECK_INDEX) {
            throw invalid("deckIndex는 0부터 21 사이여야 합니다.");
        }

        if (selection.positionCode() == null) {
            throw invalid("positionCode는 필수입니다.");
        }
    }

    private BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
