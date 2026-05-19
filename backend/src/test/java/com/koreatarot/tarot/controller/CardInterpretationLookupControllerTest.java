package com.koreatarot.tarot.controller;

import com.koreatarot.tarot.dto.CardInterpretationLookupDto;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.tarot.service.CardInterpretationLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardInterpretationLookupControllerTest {

    private CardInterpretationLookupService cardInterpretationLookupService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cardInterpretationLookupService = mock(CardInterpretationLookupService.class);
        CardInterpretationLookupController controller =
                new CardInterpretationLookupController(cardInterpretationLookupService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void lookupReturnsDocuments() throws Exception {
        when(cardInterpretationLookupService.lookup(any(CardInterpretationLookupDto.LookupRequest.class)))
                .thenReturn(new CardInterpretationLookupDto.LookupResponse(List.of(
                        document(6L, "The Lovers", PositionCode.PRESENT),
                        document(18L, "The Moon", PositionCode.OBSTACLE),
                        document(14L, "Temperance", PositionCode.ADVICE)
                )));

        mockMvc.perform(post("/internal/v1/card-interpretations/lookup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cards": [
                                    { "cardId": 6, "positionCode": "PRESENT", "orientation": "UPRIGHT" },
                                    { "cardId": 18, "positionCode": "OBSTACLE", "orientation": "UPRIGHT" },
                                    { "cardId": 14, "positionCode": "ADVICE", "orientation": "UPRIGHT" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.documents[0].documentId").value("card-6-present-tarot-doc-v1.0"))
                .andExpect(jsonPath("$.data.documents[0].cardId").value(6))
                .andExpect(jsonPath("$.data.documents[0].cardName").value("The Lovers"))
                .andExpect(jsonPath("$.data.documents[0].positionCode").value("PRESENT"))
                .andExpect(jsonPath("$.data.documents[0].orientation").value("UPRIGHT"))
                .andExpect(jsonPath("$.data.documents[0].keywords[0]").value("선택"))
                .andExpect(jsonPath("$.data.documents[0].documentVersion").value("tarot-doc-v1.0"));
    }

    private CardInterpretationLookupDto.DocumentResponse document(
            Long cardId,
            String cardName,
            PositionCode positionCode
    ) {
        return new CardInterpretationLookupDto.DocumentResponse(
                "card-%d-%s-tarot-doc-v1.0".formatted(cardId, positionCode.name().toLowerCase()),
                cardId,
                cardName,
                positionCode.name(),
                CardOrientation.UPRIGHT.name(),
                List.of("선택"),
                "카드 해석",
                "tarot-doc-v1.0"
        );
    }
}
