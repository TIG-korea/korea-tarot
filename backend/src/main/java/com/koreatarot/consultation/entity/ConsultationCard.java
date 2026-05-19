package com.koreatarot.consultation.entity;

import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "consultation_cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_id", nullable = false)
    private Long consultationId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "position_order", nullable = false)
    private Integer positionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_code", nullable = false, length = 20)
    private PositionCode positionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardOrientation orientation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private ConsultationCard(
            Long consultationId,
            Long cardId,
            Integer positionOrder,
            PositionCode positionCode,
            CardOrientation orientation
    ) {
        this.consultationId = consultationId;
        this.cardId = cardId;
        this.positionOrder = positionOrder;
        this.positionCode = positionCode;
        this.orientation = orientation;
        this.createdAt = LocalDateTime.now();
    }
}
