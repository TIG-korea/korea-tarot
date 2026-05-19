package com.koreatarot.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ai_request_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultation_id")
    private Long consultationId;

    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "error_category", length = 100)
    private String errorCategory;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    private AiRequestLog(
            Long consultationId,
            String requestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer latencyMs,
            String status,
            String errorCategory,
            String errorMessage
    ) {
        this.consultationId = consultationId;
        this.requestId = requestId;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.latencyMs = latencyMs;
        this.status = status;
        this.errorCategory = errorCategory;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }
}
