package com.koreatarot.global.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity
                .status(errorCode.status())
                .body(ProblemDetailResponse.of(errorCode, exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ProblemDetailResponse.FieldError> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ProblemDetailResponse.validation(
                        ErrorCode.VALIDATION_FAILED.title(),
                        request.getRequestURI(),
                        errors
                ));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetailResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .badRequest()
                .body(ProblemDetailResponse.validation(
                        ErrorCode.VALIDATION_FAILED.title(),
                        request.getRequestURI(),
                        List.of()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.status())
                .body(ProblemDetailResponse.of(errorCode, errorCode.title(), request.getRequestURI()));
    }

    private ProblemDetailResponse.FieldError toFieldError(FieldError fieldError) {
        String code = fieldError.getCode() == null ? ErrorCode.VALIDATION_FAILED.name() : fieldError.getCode();
        return new ProblemDetailResponse.FieldError(fieldError.getField(), code);
    }
}
