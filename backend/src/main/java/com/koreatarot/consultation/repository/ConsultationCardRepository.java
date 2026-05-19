package com.koreatarot.consultation.repository;

import com.koreatarot.consultation.entity.ConsultationCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationCardRepository extends JpaRepository<ConsultationCard, Long> {
}
