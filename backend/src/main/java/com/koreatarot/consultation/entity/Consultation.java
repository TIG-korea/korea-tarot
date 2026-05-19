package com.koreatarot.consultation.entity;

import com.koreatarot.consultation.enums.ConsultationStatus;
import com.koreatarot.consultation.enums.SpreadType;
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
@Table(name = "consultations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String concern;

    @Enumerated(EnumType.STRING)
    @Column(name = "spread_type", nullable = false, length = 50)
    private SpreadType spreadType;

    @Column(name = "category_code", length = 50)
    private String categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationStatus status;

    @Column(name = "result_summary", length = 300)
    private String resultSummary;

    @Column(name = "result_detail", columnDefinition = "JSON")
    private String resultDetail;

    @Column(name = "retrieved_doc_ids", columnDefinition = "JSON")
    private String retrievedDocIds;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "model_provider", length = 50)
    private String modelProvider;

    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "document_version", length = 50)
    private String documentVersion;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private Consultation(
            Long userId,
            String concern,
            SpreadType spreadType,
            String idempotencyKey
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.userId = userId;
        this.concern = concern;
        this.spreadType = spreadType;
        this.status = ConsultationStatus.PENDING;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void startStreaming() {
        if (this.status == ConsultationStatus.PENDING) {
            this.status = ConsultationStatus.STREAMING;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void fail() {
        if (this.status == ConsultationStatus.PENDING || this.status == ConsultationStatus.STREAMING) {
            this.status = ConsultationStatus.FAILED;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
