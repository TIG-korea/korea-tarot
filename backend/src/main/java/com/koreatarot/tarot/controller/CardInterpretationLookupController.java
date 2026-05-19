package com.koreatarot.tarot.controller;

import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.openapi.docs.TarotApiDocs;
import com.koreatarot.tarot.dto.CardInterpretationLookupDto;
import com.koreatarot.tarot.service.CardInterpretationLookupService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/card-interpretations")
@TarotApiDocs.TarotTag
public class CardInterpretationLookupController {

    private final CardInterpretationLookupService cardInterpretationLookupService;

    public CardInterpretationLookupController(CardInterpretationLookupService cardInterpretationLookupService) {
        this.cardInterpretationLookupService = cardInterpretationLookupService;
    }

    @PostMapping("/lookup")
    @TarotApiDocs.LookupCardInterpretations
    public ApiResponse<CardInterpretationLookupDto.LookupResponse> lookup(
            @Valid @RequestBody CardInterpretationLookupDto.LookupRequest request
    ) {
        return ApiResponse.success(cardInterpretationLookupService.lookup(request));
    }
}
