package com.koreatarot.tarot.dto;

import com.koreatarot.tarot.entity.CardInterpretation;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CardInterpretationLookupDto {

    public record LookupRequest(
            @NotEmpty
            @Size(min = 3, max = 3)
            List<@Valid LookupItem> cards
    ) {
    }

    public record LookupItem(
            @NotNull
            Long cardId,

            @NotNull
            PositionCode positionCode,

            @NotNull
            CardOrientation orientation
    ) {
    }

    public record LookupResponse(
            List<DocumentResponse> documents
    ) {
        public static LookupResponse from(List<DocumentResponse> documents) {
            return new LookupResponse(documents);
        }
    }

    public record DocumentResponse(
            String documentId,
            Long cardId,
            String cardName,
            String positionCode,
            String orientation,
            List<String> keywords,
            String interpretation,
            String documentVersion
    ) {
        public static DocumentResponse of(CardInterpretation interpretation, List<String> keywords) {
            return new DocumentResponse(
                    documentId(interpretation),
                    interpretation.getCard().getId(),
                    interpretation.getCard().getNameEn(),
                    interpretation.getPositionCode().name(),
                    interpretation.getOrientation().name(),
                    keywords,
                    interpretation.getInterpretation(),
                    interpretation.getDocumentVersion()
            );
        }

        private static String documentId(CardInterpretation interpretation) {
            return "card-%d-%s-%s".formatted(
                    interpretation.getCard().getId(),
                    interpretation.getPositionCode().name().toLowerCase(),
                    interpretation.getDocumentVersion()
            );
        }
    }

    private CardInterpretationLookupDto() {
    }
}
