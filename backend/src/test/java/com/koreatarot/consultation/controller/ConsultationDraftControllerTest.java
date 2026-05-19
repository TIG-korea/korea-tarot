package com.koreatarot.consultation.controller;

import com.koreatarot.consultation.dto.ConsultationDraftDto;
import com.koreatarot.consultation.service.ConcernValidator;
import com.koreatarot.consultation.service.DraftDeckService;
import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationDraftControllerTest {

    private final ConcernValidator concernValidator = mock(ConcernValidator.class);
    private final DraftDeckService draftDeckService = mock(DraftDeckService.class);
    private final ConsultationDraftController controller = new ConsultationDraftController(
            concernValidator,
            draftDeckService
    );

    @Test
    void createDraftReturnsOnlyPublicDraftMetadata() {
        String concern = "현재 만나는 사람과 관계가 고민돼요.";
        Instant expiresAt = Instant.parse("2026-05-19T10:40:00Z");
        when(concernValidator.validate(concern)).thenReturn(concern);
        when(draftDeckService.create(1L, concern))
                .thenReturn(new DraftDeckService.DraftDeckCreateResult("drf_abc123", 22, expiresAt));

        ApiResponse<ConsultationDraftDto.CreateResponse> response = controller.createDraft(
                new AuthenticatedUser(1L, "user@example.com"),
                new ConsultationDraftDto.CreateRequest(concern)
        );

        assertThat(response.success()).isTrue();
        assertThat(response.data().draftId()).isEqualTo("drf_abc123");
        assertThat(response.data().deckSize()).isEqualTo(22);
        assertThat(response.data().expiresAt()).isEqualTo(expiresAt);

        verify(concernValidator).validate(concern);
        verify(draftDeckService).create(1L, concern);
    }

    @Test
    void createDraftRejectsUnauthenticatedRequest() {
        assertThatThrownBy(() -> controller.createDraft(
                null,
                new ConsultationDraftDto.CreateRequest("현재 만나는 사람과 관계가 고민돼요.")
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    void createDraftRejectsEmptyRequestBody() {
        assertThatThrownBy(() -> controller.createDraft(
                new AuthenticatedUser(1L, "user@example.com"),
                null
        )).isInstanceOf(BusinessException.class);
    }
}
