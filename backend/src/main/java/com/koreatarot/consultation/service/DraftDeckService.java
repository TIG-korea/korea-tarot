package com.koreatarot.consultation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.tarot.enums.Arcana;
import com.koreatarot.tarot.repository.TarotCardRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DraftDeckService {

    public static final int DECK_SIZE = 22;
    public static final Duration TTL = Duration.ofMinutes(10);

    private static final String KEY_PREFIX = "consultation:draft:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TarotCardRepository tarotCardRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public DraftDeckService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            TarotCardRepository tarotCardRepository
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tarotCardRepository = tarotCardRepository;
    }

    public DraftDeckCreateResult create(Long userId, String concern) {
        List<Long> deckMapping = new ArrayList<>(tarotCardRepository.findIdsByArcanaOrderByCardNumberAsc(Arcana.MAJOR));
        if (deckMapping.size() != DECK_SIZE) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "메이저 아르카나 카드 22장이 준비되어 있지 않습니다.");
        }

        Collections.shuffle(deckMapping, secureRandom);

        String draftId = "drf_" + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plus(TTL);
        DraftDeck draftDeck = new DraftDeck(draftId, userId, concern, deckMapping, expiresAt, false);

        redisTemplate.opsForValue().set(key(draftId), serialize(draftDeck), TTL);
        return new DraftDeckCreateResult(draftId, DECK_SIZE, expiresAt);
    }

    public Optional<DraftDeck> findById(String draftId) {
        if (draftId == null || draftId.isBlank()) {
            return Optional.empty();
        }

        String value = redisTemplate.opsForValue().get(key(draftId));
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(deserialize(value));
    }

    private String serialize(DraftDeck draftDeck) {
        try {
            return objectMapper.writeValueAsString(draftDeck);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Draft Deck 저장 데이터 변환에 실패했습니다.");
        }
    }

    private DraftDeck deserialize(String value) {
        try {
            return objectMapper.readValue(value, DraftDeck.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Draft Deck 저장 데이터를 읽을 수 없습니다.");
        }
    }

    private String key(String draftId) {
        return KEY_PREFIX + draftId;
    }

    public record DraftDeckCreateResult(
            String draftId,
            int deckSize,
            Instant expiresAt
    ) {
    }

    public record DraftDeck(
            String draftId,
            Long userId,
            String concern,
            List<Long> deckMapping,
            Instant expiresAt,
            boolean used
    ) {
    }
}
