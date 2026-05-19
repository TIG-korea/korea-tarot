package com.koreatarot.consultation.service;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ConcernValidator {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 1000;

    public String validate(String concern) {
        if (concern == null) {
            throw invalid("고민은 필수입니다.");
        }

        String trimmed = concern.trim();
        if (trimmed.isBlank()) {
            throw invalid("고민은 공백만 입력할 수 없습니다.");
        }

        if (trimmed.length() < MIN_LENGTH) {
            throw invalid("고민은 최소 10자 이상이어야 합니다.");
        }

        if (trimmed.length() > MAX_LENGTH) {
            throw invalid("고민은 최대 1000자까지 입력할 수 있습니다.");
        }

        if (hasOnlyRepeatedCharacter(trimmed)) {
            throw invalid("고민은 의미 있는 내용으로 입력해야 합니다.");
        }

        return trimmed;
    }

    private boolean hasOnlyRepeatedCharacter(String value) {
        long uniqueCharacters = value.codePoints()
                .filter(character -> !Character.isWhitespace(character))
                .distinct()
                .limit(2)
                .count();

        return uniqueCharacters == 1;
    }

    private BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
