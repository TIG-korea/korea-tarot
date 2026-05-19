package com.koreatarot.consultation.dto;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record ConsultationCursor(Long id) {

    public static ConsultationCursor from(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new ConsultationCursor(null);
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return new ConsultationCursor(Long.parseLong(decoded));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "cursor 값이 올바르지 않습니다.");
        }
    }

    public static String encode(Long id) {
        if (id == null) {
            return null;
        }
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
    }
}
