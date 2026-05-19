package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.service.ConsultationEventService;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.openapi.docs.ConsultationApiDocs;
import com.koreatarot.global.security.AuthenticatedUser;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/consultations")
@ConsultationApiDocs.ConsultationTag
public class ConsultationEventController {

    private final ConsultationEventService consultationEventService;

    public ConsultationEventController(ConsultationEventService consultationEventService) {
        this.consultationEventService = consultationEventService;
    }

    @GetMapping(value = "/{consultationId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ConsultationApiDocs.SubscribeEvents
    public Flux<ServerSentEvent<Object>> subscribe(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long consultationId
    ) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return consultationEventService.stream(authenticatedUser.id(), consultationId);
    }
}
