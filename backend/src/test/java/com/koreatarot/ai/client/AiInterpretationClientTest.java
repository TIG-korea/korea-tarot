package com.koreatarot.ai.client;

import com.koreatarot.ai.config.AiProperties;
import com.koreatarot.ai.dto.AiInterpretationDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AiInterpretationClientTest {

    @Test
    void streamInterpretationCallsConfiguredEndpointAsSseRequest() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                    .body("event: token\ndata: {\"text\":\"hello\"}\n\n")
                    .build());
        };
        AiProperties aiProperties = new AiProperties(
                "http://ai.test",
                "/internal/v1/interpretations/stream",
                30
        );
        WebClient webClient = WebClient.builder()
                .baseUrl(aiProperties.baseUrl())
                .exchangeFunction(exchangeFunction)
                .build();
        AiInterpretationClient client = new AiInterpretationClient(webClient, aiProperties);

        List<String> events = client.streamInterpretation(request())
                .map(event -> event.event() + ":" + event.data())
                .collectList()
                .block();

        assertThat(capturedRequest.get().method()).isEqualTo(HttpMethod.POST);
        assertThat(capturedRequest.get().url().getPath()).isEqualTo("/internal/v1/interpretations/stream");
        assertThat(capturedRequest.get().headers().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(capturedRequest.get().headers().getAccept()).contains(MediaType.TEXT_EVENT_STREAM);
        assertThat(events).containsExactly("token:{\"text\":\"hello\"}");
    }

    private AiInterpretationDto.StreamRequest request() {
        return new AiInterpretationDto.StreamRequest(
                "req-abc-123",
                1001L,
                1L,
                "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
                "THREE_CARD",
                List.of(new AiInterpretationDto.SelectedCard(
                        6L,
                        "The Lovers",
                        "PRESENT",
                        "UPRIGHT"
                )),
                "ko"
        );
    }
}
