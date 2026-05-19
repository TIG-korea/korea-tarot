package com.koreatarot.consultation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftDeckServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private TarotCardRepository tarotCardRepository;

    private DraftDeckService draftDeckService;

    @BeforeEach
    void setUp() {
        draftDeckService = new DraftDeckService(redisTemplate, new ObjectMapper().findAndRegisterModules(), tarotCardRepository);
    }

    @Test
    @DisplayName("메이저 아르카나 22장을 셔플해 Redis에 10분 TTL로 저장한다")
    void createStoresShuffledDraftDeck() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(tarotCardRepository.findIdsByArcanaOrderByCardNumberAsc(Arcana.MAJOR))
                .thenReturn(cardIds());

        DraftDeckService.DraftDeckCreateResult result = draftDeckService.create(
                1L,
                "현재 만나는 사람과 관계가 고민돼요."
        );

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

        assertThat(result.draftId()).startsWith("drf_");
        assertThat(result.deckSize()).isEqualTo(22);
        assertThat(result.expiresAt()).isNotNull();
        assertThat(keyCaptor.getValue()).isEqualTo("consultation:draft:" + result.draftId());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(10));

        JsonNode json = new ObjectMapper().readTree(valueCaptor.getValue());
        assertThat(json.get("draftId").asText()).isEqualTo(result.draftId());
        assertThat(json.get("userId").asLong()).isEqualTo(1L);
        assertThat(json.get("concern").asText()).isEqualTo("현재 만나는 사람과 관계가 고민돼요.");
        assertThat(json.get("used").asBoolean()).isFalse();
        assertThat(json.get("deckMapping")).hasSize(22);
    }

    @Test
    @DisplayName("메이저 아르카나가 22장이 아니면 draft를 만들지 않는다")
    void createRejectsInvalidMajorArcanaSeed() {
        when(tarotCardRepository.findIdsByArcanaOrderByCardNumberAsc(Arcana.MAJOR))
                .thenReturn(List.of(1L, 2L, 3L));

        assertThatThrownBy(() -> draftDeckService.create(1L, "현재 만나는 사람과 관계가 고민돼요."))
                .isInstanceOf(BusinessException.class)
                .hasMessage("메이저 아르카나 카드 22장이 준비되어 있지 않습니다.");
    }

    @Test
    @DisplayName("Redis에 저장된 draft를 조회한다")
    void findByIdReturnsStoredDraftDeck() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        String storedValue = """
                {
                  "draftId": "drf_test",
                  "userId": 1,
                  "concern": "현재 만나는 사람과 관계가 고민돼요.",
                  "deckMapping": [1, 2, 3],
                  "expiresAt": "2026-05-19T10:40:00Z",
                  "used": false
                }
                """;
        when(valueOperations.get("consultation:draft:drf_test")).thenReturn(storedValue);

        Optional<DraftDeckService.DraftDeck> result = draftDeckService.findById("drf_test");

        assertThat(result).isPresent();
        assertThat(result.get().draftId()).isEqualTo("drf_test");
        assertThat(result.get().userId()).isEqualTo(1L);
        assertThat(result.get().deckMapping()).containsExactly(1L, 2L, 3L);
        assertThat(result.get().used()).isFalse();
    }

    @Test
    @DisplayName("draftId가 비었거나 Redis에 없으면 빈 값을 반환한다")
    void findByIdReturnsEmptyWhenDraftDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThat(draftDeckService.findById("drf_missing")).isEmpty();
        assertThat(draftDeckService.findById(" ")).isEmpty();
    }

    private List<Long> cardIds() {
        return java.util.stream.LongStream.rangeClosed(1, 22)
                .boxed()
                .toList();
    }
}
