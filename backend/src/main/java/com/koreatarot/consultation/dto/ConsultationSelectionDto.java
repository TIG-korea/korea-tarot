package com.koreatarot.consultation.dto;

import com.koreatarot.tarot.enums.PositionCode;

public final class ConsultationSelectionDto {

    public record CardSelectionRequest(
            Integer deckIndex,
            PositionCode positionCode
    ) {
    }

    private ConsultationSelectionDto() {
    }
}
