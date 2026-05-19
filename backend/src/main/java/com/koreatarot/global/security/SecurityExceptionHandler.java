package com.koreatarot.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.error.ProblemDetailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SecurityExceptionHandler {

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleAuthenticationException(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        writeProblem(response, ProblemDetailResponse.of(
                ErrorCode.UNAUTHORIZED,
                ErrorCode.UNAUTHORIZED.title(),
                request.getRequestURI()
        ));
    }

    public void handleAccessDeniedException(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        writeProblem(response, ProblemDetailResponse.of(
                ErrorCode.FORBIDDEN,
                ErrorCode.FORBIDDEN.title(),
                request.getRequestURI()
        ));
    }

    private void writeProblem(HttpServletResponse response, ProblemDetailResponse problem) throws IOException {
        response.setStatus(problem.status());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
