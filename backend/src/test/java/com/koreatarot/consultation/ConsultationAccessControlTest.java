package com.koreatarot.consultation;

import com.koreatarot.consultation.repository.ConsultationCardRepository;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.consultation.service.ConsultationHistoryService;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultationAccessControlTest {

    private final ConsultationRepository consultationRepository = mock(ConsultationRepository.class);
    private final ConsultationCardRepository consultationCardRepository = mock(ConsultationCardRepository.class);
    private final TarotCardRepository tarotCardRepository = mock(TarotCardRepository.class);
    private final ConsultationHistoryService consultationHistoryService = new ConsultationHistoryService(
            consultationRepository,
            consultationCardRepository,
            tarotCardRepository
    );

    @Test
    void getDetailRejectsOtherUserConsultation() {
        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationHistoryService.getDetail(2L, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상담을 찾을 수 없습니다.");
    }

    @Test
    void deleteRejectsOtherUserConsultation() {
        when(consultationRepository.findByIdAndUserIdAndDeletedAtIsNull(1001L, 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultationHistoryService.delete(2L, 1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("상담을 찾을 수 없습니다.");
    }
}
