package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.dto.ConsultationHistoryDto;
import com.koreatarot.consultation.service.ConsultationHistoryService;
import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.openapi.docs.ConsultationApiDocs;
import com.koreatarot.global.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/consultations")
@ConsultationApiDocs.ConsultationTag
public class ConsultationHistoryController {

    private final ConsultationHistoryService consultationHistoryService;

    public ConsultationHistoryController(ConsultationHistoryService consultationHistoryService) {
        this.consultationHistoryService = consultationHistoryService;
    }

    @GetMapping
    @ConsultationApiDocs.ListHistory
    public ApiResponse<ConsultationHistoryDto.ListResponse> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.success(consultationHistoryService.list(authenticatedUser.id(), cursor, size));
    }

    @GetMapping("/{consultationId}")
    @ConsultationApiDocs.GetHistoryDetail
    public ApiResponse<ConsultationHistoryDto.DetailResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long consultationId
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return ApiResponse.success(consultationHistoryService.getDetail(authenticatedUser.id(), consultationId));
    }

    @DeleteMapping("/{consultationId}")
    @ConsultationApiDocs.DeleteHistory
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long consultationId
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        consultationHistoryService.delete(authenticatedUser.id(), consultationId);
        return ApiResponse.empty();
    }
}
