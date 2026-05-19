package com.koreatarot.consultation.service;

import com.koreatarot.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcernValidatorTest {

    private final ConcernValidator validator = new ConcernValidator();

    @Test
    @DisplayName("유효한 고민이면 앞뒤 공백을 제거한 값을 반환한다")
    void validateReturnsTrimmedConcern() {
        String result = validator.validate("  현재 만나는 사람과 관계가 고민돼요.  ");

        assertThat(result).isEqualTo("현재 만나는 사람과 관계가 고민돼요.");
    }

    @Test
    @DisplayName("고민이 null이면 거부한다")
    void validateRejectsNullConcern() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("고민은 필수입니다.");
    }

    @Test
    @DisplayName("고민이 공백뿐이면 거부한다")
    void validateRejectsBlankConcern() {
        assertThatThrownBy(() -> validator.validate("          "))
                .isInstanceOf(BusinessException.class)
                .hasMessage("고민은 공백만 입력할 수 없습니다.");
    }

    @Test
    @DisplayName("고민이 10자 미만이면 거부한다")
    void validateRejectsTooShortConcern() {
        assertThatThrownBy(() -> validator.validate("짧은 고민"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("고민은 최소 10자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("고민이 1000자를 초과하면 거부한다")
    void validateRejectsTooLongConcern() {
        String concern = "가".repeat(1001);

        assertThatThrownBy(() -> validator.validate(concern))
                .isInstanceOf(BusinessException.class)
                .hasMessage("고민은 최대 1000자까지 입력할 수 있습니다.");
    }

    @Test
    @DisplayName("같은 문자만 반복된 고민이면 거부한다")
    void validateRejectsOnlyRepeatedCharacterConcern() {
        assertThatThrownBy(() -> validator.validate("ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("고민은 의미 있는 내용으로 입력해야 합니다.");
    }
}
