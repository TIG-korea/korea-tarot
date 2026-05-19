package com.koreatarot.consultation.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public final class ConsultationHistoryDto {

    public record ListResponse(
            List<Item> items,
            String nextCursor
    ) {
    }

    public record Item(
            Long consultationId,
            String concernPreview,
            List<String> cardNames,
            String categoryCode,
            String summary,
            String status,
            LocalDateTime createdAt
    ) {
    }

    public record DetailResponse(
            Long consultationId,
            String concern,
            String spreadType,
            String categoryCode,
            String status,
            List<Card> cards,
            JsonNode result,
            LocalDateTime createdAt
    ) {
    }

    public record Card(
            Long cardId,
            String cardNameEn,
            String cardNameKo,
            Integer positionOrder,
            String positionCode,
            String positionName,
            String orientation,
            String imageUrl
    ) {
    }

    private ConsultationHistoryDto() {
    }
}
