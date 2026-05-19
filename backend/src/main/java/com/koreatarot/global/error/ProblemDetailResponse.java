package com.koreatarot.global.error;

import java.util.List;

public record ProblemDetailResponse(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        List<FieldError> errors
) {

    public static ProblemDetailResponse of(ErrorCode errorCode, String detail, String instance) {
        return new ProblemDetailResponse(
                type(errorCode),
                errorCode.title(),
                errorCode.status().value(),
                detail,
                instance,
                List.of()
        );
    }

    public static ProblemDetailResponse validation(String detail, String instance, List<FieldError> errors) {
        return new ProblemDetailResponse(
                type(ErrorCode.VALIDATION_FAILED),
                ErrorCode.VALIDATION_FAILED.title(),
                ErrorCode.VALIDATION_FAILED.status().value(),
                detail,
                instance,
                errors
        );
    }

    private static String type(ErrorCode errorCode) {
        return "https://api.korea-tarot.example/errors/" + errorCode.name().toLowerCase();
    }

    public record FieldError(
            String field,
            String code
    ) {
    }
}
