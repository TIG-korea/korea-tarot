package com.koreatarot.tarot.controller;

import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.service.TarotCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TarotCardControllerTest {

    private TarotCardService tarotCardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tarotCardService = mock(TarotCardService.class);
        TarotCardController tarotCardController = new TarotCardController(tarotCardService);
        mockMvc = MockMvcBuilders.standaloneSetup(tarotCardController).build();
    }

    @Test
    void listCardsReturnsTarotCardList() throws Exception {
        when(tarotCardService.listMajorCards()).thenReturn(List.of(theFool()));

        mockMvc.perform(get("/api/v1/tarot/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cards[0].id").value(1))
                .andExpect(jsonPath("$.data.cards[0].nameEn").value("The Fool"))
                .andExpect(jsonPath("$.data.cards[0].nameKo").value("광대"))
                .andExpect(jsonPath("$.data.cards[0].arcana").value("MAJOR"))
                .andExpect(jsonPath("$.data.cards[0].cardNumber").value(0))
                .andExpect(jsonPath("$.data.cards[0].imageUrl").value("/images/cards/major-00-fool.png"));
    }

    private TarotCard theFool() {
        TarotCard tarotCard = TarotCard.builder()
                .nameEn("The Fool")
                .nameKo("광대")
                .arcana(Arcana.MAJOR)
                .cardNumber(0)
                .imageUrl("/images/cards/major-00-fool.png")
                .build();
        ReflectionTestUtils.setField(tarotCard, "id", 1L);
        return tarotCard;
    }
}
