package com.koreatarot.tarot.controller;

import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.openapi.docs.TarotApiDocs;
import com.koreatarot.tarot.dto.TarotCardDto;
import com.koreatarot.tarot.service.TarotCardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tarot/cards")
@TarotApiDocs.TarotTag
public class TarotCardController {

    private final TarotCardService tarotCardService;

    public TarotCardController(TarotCardService tarotCardService) {
        this.tarotCardService = tarotCardService;
    }

    @GetMapping
    @TarotApiDocs.ListCards
    public ApiResponse<TarotCardDto.ListResponse> listCards() {
        return ApiResponse.success(TarotCardDto.ListResponse.from(tarotCardService.listMajorCards()));
    }
}
