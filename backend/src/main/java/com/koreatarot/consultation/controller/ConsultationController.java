package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.dto.ConsultationSelectionDto;
import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.service.ConsultationService;
import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.openapi.docs.ConsultationApiDocs;
import com.koreatarot.global.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultations")
@ConsultationApiDocs.ConsultationTag
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    @ConsultationApiDocs.CreateConsultation
    public ApiResponse<ConsultationSelectionDto.CreateResponse> createConsultation(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody ConsultationSelectionDto.CreateRequest request
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Consultation consultation = consultationService.create(authenticatedUser.id(), idempotencyKey, request);

        return ApiResponse.success(ConsultationSelectionDto.CreateResponse.of(
                consultation.getId(),
                consultation.getStatus().name()
        ));
    }
}
