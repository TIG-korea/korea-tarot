package com.koreatarot.tarot.entity;

import com.koreatarot.tarot.enums.Arcana;
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
@Table(name = "tarot_cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TarotCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_ko", nullable = false, length = 100)
    private String nameKo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Arcana arcana;

    @Column(length = 20)
    private String suit;

    @Column(name = "card_number")
    private Integer cardNumber;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private TarotCard(
            String nameEn,
            String nameKo,
            Arcana arcana,
            String suit,
            Integer cardNumber,
            String imageUrl
    ) {
        this.nameEn = nameEn;
        this.nameKo = nameKo;
        this.arcana = arcana;
        this.suit = suit;
        this.cardNumber = cardNumber;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }
}
