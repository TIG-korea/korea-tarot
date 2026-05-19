package com.koreatarot.global.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String REFRESH_COOKIE = "refreshTokenCookie";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server().url("http://localhost:8080").description("로컬 백엔드 서버"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerAuthScheme())
                        .addSecuritySchemes(REFRESH_COOKIE, refreshCookieScheme()))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    private Info apiInfo() {
        return new Info()
                .title("Korea Tarot API")
                .description("""
                        AI 기반 타로 상담 서비스 MVP API 문서입니다.

                        모든 성공 응답은 기본적으로 ApiResponse<T> 형태를 사용하고,
                        오류 응답은 RFC 7807 Problem Details 구조를 따릅니다.
                        Swagger UI의 테스트 실행 기능은 로컬 검증 편의를 위해 활성화합니다.
                        """)
                .version("v1")
                .license(new License().name("Private"));
    }

    private SecurityScheme bearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access Token을 Authorization: Bearer <token> 형식으로 전달합니다.");
    }

    private SecurityScheme refreshCookieScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("refreshToken")
                .description("HttpOnly Secure refresh token cookie입니다.");
    }
}
