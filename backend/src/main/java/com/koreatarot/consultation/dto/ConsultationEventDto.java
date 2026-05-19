package com.koreatarot.consultation.dto;

import java.util.List;

public final class ConsultationEventDto {

    public record MetaEvent(
            Long consultationId,
            List<CardMeta> cards
    ) {
    }

    public record CardMeta(
            Long cardId,
            String cardName,
            String positionCode
    ) {
    }

    public record ErrorEvent(
            String code,
            String message
    ) {
    }

    private ConsultationEventDto() {
    }
}
