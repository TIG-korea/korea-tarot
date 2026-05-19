package com.koreatarot.tarot.entity;

import com.koreatarot.tarot.enums.CardOrientation;
import com.koreatarot.tarot.enums.PositionCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "card_interpretations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardInterpretation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private TarotCard card;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardOrientation orientation;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_code", nullable = false, length = 20)
    private PositionCode positionCode;

    @Column(nullable = false, columnDefinition = "json")
    private String keywords;

    @Column(nullable = false, columnDefinition = "text")
    private String interpretation;

    @Column(name = "document_version", nullable = false, length = 20)
    private String documentVersion;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private CardInterpretation(
            TarotCard card,
            CardOrientation orientation,
            PositionCode positionCode,
            String keywords,
            String interpretation,
            String documentVersion,
            boolean active
    ) {
        this.card = card;
        this.orientation = orientation;
        this.positionCode = positionCode;
        this.keywords = keywords;
        this.interpretation = interpretation;
        this.documentVersion = documentVersion;
        this.active = active;
        this.createdAt = LocalDateTime.now();
    }
}
