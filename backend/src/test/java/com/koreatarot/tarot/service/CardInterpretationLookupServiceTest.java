package com.koreatarot.tarot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.tarot.dto.CardInterpretationLookupDto;
import com.koreatarot.tarot.entity.CardInterpretation;
import com.koreatarot.tarot.entity.TarotCard;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import com.koreatarot.tarot.repository.CardInterpretationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardInterpretationLookupServiceTest {

    private CardInterpretationRepository cardInterpretationRepository;
    private CardInterpretationLookupService cardInterpretationLookupService;

    @BeforeEach
    void setUp() {
        cardInterpretationRepository = mock(CardInterpretationRepository.class);
        cardInterpretationLookupService = new CardInterpretationLookupService(
                cardInterpretationRepository,
                new ObjectMapper()
        );
    }

    @Test
    void lookupReturnsCardInterpretationDocuments() {
        CardInterpretation interpretation = interpretation(theLovers(), PositionCode.PRESENT);
        when(cardInterpretationRepository.findTopByCard_IdAndOrientationAndPositionCodeAndActiveTrueOrderByDocumentVersionDesc(
                6L,
                CardOrientation.UPRIGHT,
                PositionCode.PRESENT
        )).thenReturn(Optional.of(interpretation));

        CardInterpretationLookupDto.LookupResponse response = cardInterpretationLookupService.lookup(
                new CardInterpretationLookupDto.LookupRequest(List.of(
                        new CardInterpretationLookupDto.LookupItem(6L, PositionCode.PRESENT, CardOrientation.UPRIGHT)
                ))
        );

        CardInterpretationLookupDto.DocumentResponse document = response.documents().getFirst();
        assertThat(document.documentId()).isEqualTo("card-6-present-tarot-doc-v1.0");
        assertThat(document.cardName()).isEqualTo("The Lovers");
        assertThat(document.positionCode()).isEqualTo("PRESENT");
        assertThat(document.orientation()).isEqualTo("UPRIGHT");
        assertThat(document.keywords()).containsExactly("선택", "관계", "현재");
        assertThat(document.interpretation()).isEqualTo("현재 상황 해석");
        assertThat(document.documentVersion()).isEqualTo("tarot-doc-v1.0");
    }

    @Test
    void lookupThrowsWhenDocumentDoesNotExist() {
        when(cardInterpretationRepository.findTopByCard_IdAndOrientationAndPositionCodeAndActiveTrueOrderByDocumentVersionDesc(
                6L,
                CardOrientation.UPRIGHT,
                PositionCode.PRESENT
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardInterpretationLookupService.lookup(
                new CardInterpretationLookupDto.LookupRequest(List.of(
                        new CardInterpretationLookupDto.LookupItem(6L, PositionCode.PRESENT, CardOrientation.UPRIGHT)
                ))
        )).isInstanceOf(BusinessException.class)
                .hasMessage("카드 해석 문서를 찾을 수 없습니다.");
    }

    private CardInterpretation interpretation(TarotCard card, PositionCode positionCode) {
        CardInterpretation interpretation = CardInterpretation.builder()
                .card(card)
                .orientation(CardOrientation.UPRIGHT)
                .positionCode(positionCode)
                .keywords("[\"선택\",\"관계\",\"현재\"]")
                .interpretation("현재 상황 해석")
                .documentVersion("tarot-doc-v1.0")
                .active(true)
                .build();
        ReflectionTestUtils.setField(interpretation, "id", 100L);
        return interpretation;
    }

    private TarotCard theLovers() {
        TarotCard card = TarotCard.builder()
                .nameEn("The Lovers")
                .nameKo("연인")
                .arcana(Arcana.MAJOR)
                .cardNumber(6)
                .imageUrl("/images/cards/major-06-lovers.png")
                .build();
        ReflectionTestUtils.setField(card, "id", 6L);
        return card;
    }
}
