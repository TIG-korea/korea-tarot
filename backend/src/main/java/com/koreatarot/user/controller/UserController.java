package com.koreatarot.user.controller;

import com.koreatarot.global.api.ApiResponse;
import com.koreatarot.global.error.BusinessException;
import com.koreatarot.global.error.ErrorCode;
import com.koreatarot.global.openapi.docs.UserApiDocs;
import com.koreatarot.global.security.AuthenticatedUser;
import com.koreatarot.user.dto.UserDto;
import com.koreatarot.user.entity.User;
import com.koreatarot.user.repository.UserRepository;
import com.koreatarot.user.service.UserWithdrawalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@UserApiDocs.UserTag
public class UserController {

    private final UserRepository userRepository;
    private final UserWithdrawalService userWithdrawalService;

    public UserController(
            UserRepository userRepository,
            UserWithdrawalService userWithdrawalService
    ) {
        this.userRepository = userRepository;
        this.userWithdrawalService = userWithdrawalService;
    }

    @GetMapping("/me")
    @UserApiDocs.GetProfile
    public ApiResponse<UserDto.ProfileResponse> me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        return ApiResponse.success(UserDto.ProfileResponse.from(user));
    }

    @PostMapping("/me/withdrawal")
    @UserApiDocs.Withdraw
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        userWithdrawalService.requestWithdrawal(authenticatedUser.id());
        return ApiResponse.empty();
    }
}
