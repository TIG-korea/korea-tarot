package com.koreatarot.tarot.repository;

import com.koreatarot.tarot.entity.CardInterpretation;
import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardInterpretationRepository extends JpaRepository<CardInterpretation, Long> {

    Optional<CardInterpretation> findTopByCard_IdAndOrientationAndPositionCodeAndActiveTrueOrderByDocumentVersionDesc(
            Long cardId,
            CardOrientation orientation,
            PositionCode positionCode
    );
}
