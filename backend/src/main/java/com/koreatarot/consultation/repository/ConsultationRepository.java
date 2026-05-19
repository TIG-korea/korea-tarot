package com.koreatarot.consultation.repository;

import com.koreatarot.consultation.entity.Consultation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    Optional<Consultation> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("""
            select c
            from Consultation c
            where c.userId = :userId
              and c.deletedAt is null
              and (:cursorId is null or c.id < :cursorId)
            order by c.createdAt desc, c.id desc
            """)
    List<Consultation> findHistory(
            @Param("userId") Long userId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
