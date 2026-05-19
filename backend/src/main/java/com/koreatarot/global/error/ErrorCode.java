package com.koreatarot.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값 검증 실패"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "요청이 현재 상태와 충돌합니다"),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "요청 제한"),
    GONE(HttpStatus.GONE, "만료되었거나 더 이상 사용할 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다");

    private final HttpStatus status;
    private final String title;

    ErrorCode(HttpStatus status, String title) {
        this.status = status;
        this.title = title;
    }

    public HttpStatus status() {
        return status;
    }

    public String title() {
        return title;
    }
}
