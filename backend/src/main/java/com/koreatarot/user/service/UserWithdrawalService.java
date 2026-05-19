package com.koreatarot.user.service;

import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserWithdrawalService {

    private final UserRepository userRepository;

    public UserWithdrawalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void requestWithdrawal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        user.requestWithdrawal();
        userRepository.save(user);
    }
}
