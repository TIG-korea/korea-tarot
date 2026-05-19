package com.koreatarot.global.openapi.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class UserApiDocs {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Tag(name = "사용자", description = "내 정보 조회와 회원 탈퇴 API")
    public @interface UserTag {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "내 프로필 조회", description = "현재 인증된 사용자의 기본 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = OpenApiExamples.PROFILE_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public @interface GetProfile {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "회원 탈퇴 요청", description = "현재 인증된 사용자의 탈퇴 요청을 접수합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 요청 접수"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public @interface Withdraw {
    }

    private UserApiDocs() {
    }
}
