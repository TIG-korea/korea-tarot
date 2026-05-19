package com.koreatarot.global.openapi.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class TarotApiDocs {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Tag(name = "타로 카드", description = "타로 카드 표시 데이터 API")
    public @interface TarotTag {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "타로 카드 목록 조회", description = "카드 도감 또는 완료된 리딩 표시에 사용할 메이저 아르카나 카드 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = OpenApiExamples.CARD_LIST_RESPONSE)))
    public @interface ListCards {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "카드 해석 문서 내부 조회", description = "AI 서버가 선택 카드 3장의 위치별 활성 해석 문서를 조회할 때 사용하는 내부 API입니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = OpenApiExamples.CARD_INTERPRETATION_LOOKUP_RESPONSE)))
    public @interface LookupCardInterpretations {
    }

    private TarotApiDocs() {
    }
}
