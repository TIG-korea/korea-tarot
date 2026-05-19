package com.koreatarot.ai.client;

import com.koreatarot.ai.config.AiProperties;
import com.koreatarot.ai.dto.AiInterpretationDto;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
public class AiInterpretationClient {

    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;
    private final AiProperties aiProperties;

    public AiInterpretationClient(WebClient.Builder webClientBuilder, AiProperties aiProperties) {
        this(webClientBuilder.baseUrl(aiProperties.baseUrl()).build(), aiProperties);
    }

    AiInterpretationClient(WebClient webClient, AiProperties aiProperties) {
        this.webClient = webClient;
        this.aiProperties = aiProperties;
    }

    public Flux<ServerSentEvent<String>> streamInterpretation(AiInterpretationDto.StreamRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "AI 해석 요청 본문은 필수입니다.");
        }

        return webClient.post()
                .uri(aiProperties.streamPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> new BusinessException(
                                ErrorCode.INTERNAL_SERVER_ERROR,
                                "AI 서버 해석 요청에 실패했습니다."
                        )))
                .bodyToFlux(SSE_TYPE)
                .timeout(Duration.ofSeconds(aiProperties.timeoutSeconds()));
    }
}
