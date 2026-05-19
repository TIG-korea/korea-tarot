package com.koreatarot.tarot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.dto.CardInterpretationLookupDto;
import com.koreatarot.tarot.entity.CardInterpretation;
import com.koreatarot.tarot.repository.CardInterpretationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class CardInterpretationLookupService {

    private static final TypeReference<List<String>> KEYWORDS_TYPE = new TypeReference<>() {
    };

    private final CardInterpretationRepository cardInterpretationRepository;
    private final ObjectMapper objectMapper;

    public CardInterpretationLookupService(
            CardInterpretationRepository cardInterpretationRepository,
            ObjectMapper objectMapper
    ) {
        this.cardInterpretationRepository = cardInterpretationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CardInterpretationLookupDto.LookupResponse lookup(CardInterpretationLookupDto.LookupRequest request) {
        List<CardInterpretationLookupDto.DocumentResponse> documents = request.cards().stream()
                .map(this::lookupOne)
                .toList();
        return CardInterpretationLookupDto.LookupResponse.from(documents);
    }

    private CardInterpretationLookupDto.DocumentResponse lookupOne(CardInterpretationLookupDto.LookupItem item) {
        CardInterpretation interpretation = cardInterpretationRepository
                .findTopByCard_IdAndOrientationAndPositionCodeAndActiveTrueOrderByDocumentVersionDesc(
                        item.cardId(),
                        item.orientation(),
                        item.positionCode()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카드 해석 문서를 찾을 수 없습니다."));

        return CardInterpretationLookupDto.DocumentResponse.of(interpretation, parseKeywords(interpretation));
    }

    private List<String> parseKeywords(CardInterpretation interpretation) {
        try {
            return objectMapper.readValue(interpretation.getKeywords(), KEYWORDS_TYPE);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카드 해석 문서 키워드를 읽을 수 없습니다.");
        }
    }
}
