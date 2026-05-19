package com.koreatarot.consultation.service;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.tarot.enums.PositionCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardSelectionValidatorTest {

    private final CardSelectionValidator validator = new CardSelectionValidator();

    @Test
    void validateAcceptsThreeDifferentDeckIndexesAndPositions() {
        assertThatCode(() -> validator.validate(List.of(
                selection(4, PositionCode.PRESENT),
                selection(11, PositionCode.OBSTACLE),
                selection(17, PositionCode.ADVICE)
        ))).doesNotThrowAnyException();
    }

    @Test
    void validateRejectsSelectionCountOtherThanThree() {
        assertThatThrownBy(() -> validator.validate(List.of(
                selection(4, PositionCode.PRESENT),
                selection(11, PositionCode.OBSTACLE)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("카드는 정확히 3장을 선택해야 합니다.");
    }

    @Test
    void validateRejectsDuplicateDeckIndex() {
        assertThatThrownBy(() -> validator.validate(List.of(
                selection(4, PositionCode.PRESENT),
                selection(4, PositionCode.OBSTACLE),
                selection(17, PositionCode.ADVICE)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("같은 카드를 중복 선택할 수 없습니다.");
    }

    @Test
    void validateRejectsDuplicatePositionCode() {
        assertThatThrownBy(() -> validator.validate(List.of(
                selection(4, PositionCode.PRESENT),
                selection(11, PositionCode.PRESENT),
                selection(17, PositionCode.ADVICE)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("같은 위치에 여러 카드를 배정할 수 없습니다.");
    }

    @Test
    void validateRejectsOutOfRangeDeckIndex() {
        assertThatThrownBy(() -> validator.validate(List.of(
                selection(-1, PositionCode.PRESENT),
                selection(11, PositionCode.OBSTACLE),
                selection(17, PositionCode.ADVICE)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("deckIndex는 0부터 21 사이여야 합니다.");
    }

    @Test
    void validateRejectsMissingPositionCode() {
        assertThatThrownBy(() -> validator.validate(List.of(
                selection(4, PositionCode.PRESENT),
                selection(11, null),
                selection(17, PositionCode.ADVICE)
        )))
                .isInstanceOf(BusinessException.class)
                .hasMessage("positionCode는 필수입니다.");
    }

    private ConsultationSelectionDto.CardSelectionRequest selection(Integer deckIndex, PositionCode positionCode) {
        return new ConsultationSelectionDto.CardSelectionRequest(deckIndex, positionCode);
    }
}
