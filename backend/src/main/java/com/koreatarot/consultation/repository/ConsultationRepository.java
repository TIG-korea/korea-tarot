package com.koreatarot.consultation.repository;

import com.koreatarot.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<Consultation> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
