package com.koreatarot.global.openapi.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ConsultationApiDocs {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Tag(name = "상담", description = "Draft Deck, 상담 생성, SSE, 기록 API")
    public @interface ConsultationTag {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Draft Deck 생성", description = "유효한 고민을 받아 10분 동안 사용할 수 있는 서버 셔플 draft deck을 생성합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "Draft 생성 요청", value = OpenApiExamples.DRAFT_REQUEST))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Draft 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = OpenApiExamples.DRAFT_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "고민 입력값 검증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = OpenApiExamples.PROBLEM_VALIDATION))),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public @interface CreateDraft {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "상담 생성", description = "Draft와 카드 선택 정보를 검증하고 상담을 PENDING 상태로 생성합니다.")
    @Parameter(name = "Idempotency-Key", description = "동일 상담 생성 요청의 중복 생성을 방지하는 key", required = true)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "상담 생성 요청", value = OpenApiExamples.CREATE_CONSULTATION_REQUEST))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상담 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = OpenApiExamples.CREATE_CONSULTATION_RESPONSE))),
            @ApiResponse(responseCode = "400", description = "선택값 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "410", description = "만료된 draft")
    })
    public @interface CreateConsultation {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "상담 결과 SSE 구독", description = "상담 ID 기준으로 meta, token, done, error SSE 이벤트를 구독합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SSE 구독 시작",
                    content = @Content(mediaType = "text/event-stream",
                            examples = {
                                    @ExampleObject(name = "SSE 이벤트", value = OpenApiExamples.CONSULTATION_SSE_EVENTS),
                                    @ExampleObject(name = "SSE 오류 이벤트", value = OpenApiExamples.CONSULTATION_SSE_ERROR)
                            })),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 상담 접근"),
            @ApiResponse(responseCode = "404", description = "상담 없음")
    })
    public @interface SubscribeEvents {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "상담 기록 목록 조회", description = "현재 사용자의 상담 기록을 최신순 cursor pagination으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = OpenApiExamples.HISTORY_LIST_RESPONSE)))
    public @interface ListHistory {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "상담 기록 상세 조회", description = "현재 사용자가 소유한 상담 기록의 상세 결과를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = OpenApiExamples.HISTORY_DETAIL_RESPONSE))),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 상담 접근"),
            @ApiResponse(responseCode = "404", description = "상담 없음")
    })
    public @interface GetHistoryDetail {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "상담 기록 삭제", description = "현재 사용자가 소유한 상담 기록을 기본 목록에서 숨기도록 soft delete 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 상담 접근"),
            @ApiResponse(responseCode = "404", description = "상담 없음")
    })
    public @interface DeleteHistory {
    }

    private ConsultationApiDocs() {
    }
}
