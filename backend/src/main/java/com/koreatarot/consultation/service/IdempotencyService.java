package com.koreatarot.consultation.service;

import com.koreatarot.consultation.entity.Consultation;
import com.koreatarot.consultation.repository.ConsultationRepository;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final ConsultationRepository consultationRepository;

    public IdempotencyService(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    public String validate(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Idempotency-Key 헤더는 필수입니다.");
        }
        if (idempotencyKey.length() > 100) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Idempotency-Key는 100자를 초과할 수 없습니다.");
        }
        return idempotencyKey.trim();
    }

    public Optional<Consultation> findExisting(Long userId, String idempotencyKey) {
        return consultationRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    }
}
