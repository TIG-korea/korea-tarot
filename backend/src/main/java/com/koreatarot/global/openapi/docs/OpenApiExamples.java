package com.koreatarot.global.openapi.docs;

public final class OpenApiExamples {

    public static final String SIGNUP_REQUEST = """
            {
              "email": "user@example.com",
              "password": "Password123!",
              "nickname": "tarouser",
              "termsAgreed": true,
              "privacyAgreed": true,
              "marketingAgreed": false
            }
            """;

    public static final String SIGNUP_RESPONSE = """
            {
              "success": true,
              "data": {
                "userId": 1,
                "email": "user@example.com",
                "nickname": "tarouser",
                "accessToken": "eyJhbGciOi..."
              },
              "error": null
            }
            """;

    public static final String LOGIN_REQUEST = """
            {
              "email": "user@example.com",
              "password": "Password123!"
            }
            """;

    public static final String LOGIN_RESPONSE = """
            {
              "success": true,
              "data": {
                "accessToken": "eyJhbGciOi...",
                "user": {
                  "id": 1,
                  "email": "user@example.com",
                  "nickname": "tarouser"
                }
              },
              "error": null
            }
            """;

    public static final String PROFILE_RESPONSE = """
            {
              "success": true,
              "data": {
                "id": 1,
                "email": "user@example.com",
                "nickname": "tarouser",
                "status": "ACTIVE",
                "createdAt": "2026-05-19T10:00:00Z"
              },
              "error": null
            }
            """;

    public static final String CARD_LIST_RESPONSE = """
            {
              "success": true,
              "data": {
                "cards": [
                  {
                    "id": 1,
                    "nameEn": "The Fool",
                    "nameKo": "광대",
                    "arcana": "MAJOR",
                    "cardNumber": 0,
                    "imageUrl": "/images/cards/major-00-fool.png"
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String DRAFT_REQUEST = """
            {
              "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요."
            }
            """;

    public static final String DRAFT_RESPONSE = """
            {
              "success": true,
              "data": {
                "draftId": "drf_abc123",
                "deckSize": 22,
                "expiresAt": "2026-05-19T10:40:00Z"
              },
              "error": null
            }
            """;

    public static final String CREATE_CONSULTATION_REQUEST = """
            {
              "draftId": "drf_abc123",
              "selections": [
                { "deckIndex": 4, "positionCode": "PRESENT" },
                { "deckIndex": 11, "positionCode": "OBSTACLE" },
                { "deckIndex": 17, "positionCode": "ADVICE" }
              ]
            }
            """;

    public static final String CREATE_CONSULTATION_RESPONSE = """
            {
              "success": true,
              "data": {
                "consultationId": 1001,
                "status": "PENDING",
                "streamUrl": "/api/v1/consultations/1001/events"
              },
              "error": null
            }
            """;

    public static final String CONSULTATION_SSE_EVENTS = """
            event: meta
            data: {"consultationId":1001,"cards":[{"cardId":6,"cardName":"The Lovers","positionCode":"PRESENT"}]}

            event: token
            data: {"text":"이번 리딩은 관계의 중요한 선택을 보여줍니다."}

            event: done
            data: {"result":{"summary":"관계에 대한 중요한 선택의 시기입니다.","overall":"...","cards":[],"advice":"...","caution":"..."}}
            """;

    public static final String CONSULTATION_SSE_ERROR = """
            event: error
            data: {"code":"AI_GENERATION_FAILED","message":"해석 생성에 실패했습니다. 잠시 후 다시 시도해주세요."}
            """;

    public static final String HISTORY_LIST_RESPONSE = """
            {
              "success": true,
              "data": {
                "items": [
                  {
                    "consultationId": 1001,
                    "concernPreview": "현재 만나는 사람과 관계를 계속...",
                    "cardNames": ["The Lovers", "The Moon", "Temperance"],
                    "categoryCode": "love",
                    "summary": "관계는 중요한 선택의 지점에 있습니다.",
                    "status": "COMPLETED",
                    "createdAt": "2026-05-19T10:30:00Z"
                  }
                ],
                "nextCursor": "eyJpZCI6OTk5fQ"
              },
              "error": null
            }
            """;

    public static final String HISTORY_DETAIL_RESPONSE = """
            {
              "success": true,
              "data": {
                "consultationId": 1001,
                "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
                "spreadType": "THREE_CARD",
                "categoryCode": "love",
                "status": "COMPLETED",
                "cards": [
                  {
                    "cardId": 6,
                    "cardNameEn": "The Lovers",
                    "cardNameKo": "연인",
                    "positionOrder": 1,
                    "positionCode": "PRESENT",
                    "positionName": "현재 상황",
                    "orientation": "UPRIGHT",
                    "imageUrl": "/images/cards/lovers.png"
                  }
                ],
                "result": {
                  "summary": "관계에 대한 중요한 선택의 시기입니다.",
                  "overall": "...",
                  "cards": [],
                  "advice": "...",
                  "caution": "..."
                },
                "createdAt": "2026-05-19T10:30:00Z"
              },
              "error": null
            }
            """;

    public static final String PROBLEM_VALIDATION = """
            {
              "type": "https://api.korea-tarot.example/errors/validation",
              "title": "입력값 검증 실패",
              "status": 400,
              "detail": "concern은 최소 10자 이상이어야 합니다.",
              "instance": "/api/v1/consultations/draft",
              "errors": [
                {
                  "field": "concern",
                  "code": "TOO_SHORT"
                }
              ]
            }
            """;

    private OpenApiExamples() {
    }
}
