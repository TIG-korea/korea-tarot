package com.koreatarot.auth;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    public void validate(String password) {
        if (!StringUtils.hasText(password) || password.length() < MIN_LENGTH) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "비밀번호는 최소 8자 이상이어야 합니다.");
        }
    }
}
