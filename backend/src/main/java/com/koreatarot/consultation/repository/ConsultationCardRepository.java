package com.koreatarot.consultation.repository;

import com.koreatarot.consultation.entity.ConsultationCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationCardRepository extends JpaRepository<ConsultationCard, Long> {

    List<ConsultationCard> findByConsultationIdOrderByPositionOrderAsc(Long consultationId);
}
