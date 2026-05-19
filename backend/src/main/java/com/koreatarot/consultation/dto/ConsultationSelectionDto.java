package com.koreatarot.consultation.dto;

import com.koreatarot.tarot.enums.PositionCode;

import java.util.List;

public final class ConsultationSelectionDto {

    public record CreateRequest(
            String draftId,
            List<CardSelectionRequest> selections
    ) {
    }

    public record CardSelectionRequest(
            Integer deckIndex,
            PositionCode positionCode
    ) {
    }

    public record CreateResponse(
            Long consultationId,
            String status,
            String streamUrl
    ) {

        public static CreateResponse of(Long consultationId, String status) {
            return new CreateResponse(
                    consultationId,
                    status,
                    "/api/v1/consultations/" + consultationId + "/events"
            );
        }
    }

    private ConsultationSelectionDto() {
    }
}
