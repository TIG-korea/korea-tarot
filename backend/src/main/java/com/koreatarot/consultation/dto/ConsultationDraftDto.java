package com.koreatarot.consultation.dto;

import com.koreatarot.consultation.service.DraftDeckService;

import java.time.Instant;

public final class ConsultationDraftDto {

    public record CreateRequest(
            String concern
    ) {
    }

    public record CreateResponse(
            String draftId,
            int deckSize,
            Instant expiresAt
    ) {

        public static CreateResponse from(DraftDeckService.DraftDeckCreateResult result) {
            return new CreateResponse(result.draftId(), result.deckSize(), result.expiresAt());
        }
    }

    private ConsultationDraftDto() {
    }
}
