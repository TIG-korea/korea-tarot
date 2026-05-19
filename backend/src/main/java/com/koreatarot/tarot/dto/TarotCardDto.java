package com.koreatarot.tarot.dto;

import com.koreatarot.tarot.entity.TarotCard;

import java.util.List;

public final class TarotCardDto {

    public record ListResponse(
            List<CardResponse> cards
    ) {
        public static ListResponse from(List<TarotCard> cards) {
            return new ListResponse(cards.stream()
                    .map(CardResponse::from)
                    .toList());
        }
    }

    public record CardResponse(
            Long id,
            String nameEn,
            String nameKo,
            String arcana,
            Integer cardNumber,
            String imageUrl
    ) {
        public static CardResponse from(TarotCard card) {
            return new CardResponse(
                    card.getId(),
                    card.getNameEn(),
                    card.getNameKo(),
                    card.getArcana().name(),
                    card.getCardNumber(),
                    card.getImageUrl()
            );
        }
    }

    private TarotCardDto() {
    }
}
