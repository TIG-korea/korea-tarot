package com.koreatarot.ai.dto;

import java.util.List;

public final class AiInterpretationDto {

    public record StreamRequest(
            String requestId,
            Long consultationId,
            Long userId,
            String concern,
            String spreadType,
            List<SelectedCard> cards,
            String locale
    ) {
    }

    public record SelectedCard(
            Long cardId,
            String cardName,
            String positionCode,
            String orientation
    ) {
    }

    private AiInterpretationDto() {
    }
}
