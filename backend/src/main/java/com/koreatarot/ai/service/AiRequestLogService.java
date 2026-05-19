package com.koreatarot.ai.service;

import com.koreatarot.ai.entity.AiRequestLog;
import com.koreatarot.ai.repository.AiRequestLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiRequestLogService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final AiRequestLogRepository aiRequestLogRepository;

    public AiRequestLogService(AiRequestLogRepository aiRequestLogRepository) {
        this.aiRequestLogRepository = aiRequestLogRepository;
    }

    @Transactional
    public void logSuccess(
            Long consultationId,
            String requestId,
            Integer inputTokens,
            Integer outputTokens,
            Integer latencyMs
    ) {
        aiRequestLogRepository.save(AiRequestLog.builder()
                .consultationId(consultationId)
                .requestId(requestId)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .latencyMs(latencyMs)
                .status(STATUS_SUCCESS)
                .build());
    }

    @Transactional
    public void logFailure(
            Long consultationId,
            String requestId,
            String errorCategory,
            String errorMessage
    ) {
        aiRequestLogRepository.save(AiRequestLog.builder()
                .consultationId(consultationId)
                .requestId(requestId == null || requestId.isBlank() ? "unknown" : requestId)
                .status(STATUS_FAILED)
                .errorCategory(errorCategory)
                .errorMessage(errorMessage)
                .build());
    }
}
