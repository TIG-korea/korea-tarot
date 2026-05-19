package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.dto.ConsultationDraftDto;
import com.koreatarot.consultation.service.ConcernValidator;
import com.koreatarot.consultation.service.DraftDeckService;
import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.openapi.docs.ConsultationApiDocs;
import com.koreatarot.global.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultations")
@ConsultationApiDocs.ConsultationTag
public class ConsultationDraftController {

    private final ConcernValidator concernValidator;
    private final DraftDeckService draftDeckService;

    public ConsultationDraftController(
            ConcernValidator concernValidator,
            DraftDeckService draftDeckService
    ) {
        this.concernValidator = concernValidator;
        this.draftDeckService = draftDeckService;
    }

    @PostMapping("/draft")
    @ConsultationApiDocs.CreateDraft
    public ApiResponse<ConsultationDraftDto.CreateResponse> createDraft(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody ConsultationDraftDto.CreateRequest request
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "요청 본문은 필수입니다.");
        }

        String concern = concernValidator.validate(request.concern());
        DraftDeckService.DraftDeckCreateResult result = draftDeckService.create(authenticatedUser.id(), concern);

        return ApiResponse.success(ConsultationDraftDto.CreateResponse.from(result));
    }
}
