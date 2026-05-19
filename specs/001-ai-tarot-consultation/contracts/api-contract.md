# API 계약: AI 타로 상담 서비스 MVP

모든 공개 백엔드 API는 `/api/v1` 접두 경로를 사용한다. 성공 응답은 별도 명시가 없으면 `ApiResponse<T>`로 감싼다. 오류 응답은 RFC 7807 Problem Details 형태를 따른다.

## 공통 응답 형태

### ApiResponse

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

### Problem Details

```json
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
```

## 인증 API

### 회원가입

`POST /api/v1/auth/signup`

요청:

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "nickname": "tarouser",
  "termsAgreed": true,
  "privacyAgreed": true,
  "marketingAgreed": false
}
```

응답 데이터:

```json
{
  "userId": 1,
  "email": "user@example.com",
  "nickname": "tarouser",
  "accessToken": "eyJhbGciOi..."
}
```

규칙:

- 이메일이 이미 등록되어 있으면 409를 반환한다.
- refresh token은 HttpOnly Secure SameSite=Lax cookie로 설정한다.

### 로그인

`POST /api/v1/auth/login`

요청:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

응답 데이터:

```json
{
  "accessToken": "eyJhbGciOi...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "tarouser"
  }
}
```

규칙:

- 로그인 실패 메시지는 일반화한다.
- 실패 시도는 요청 출처와 이메일 기준으로 제한한다.

### 토큰 갱신

`POST /api/v1/auth/refresh`

규칙:

- Redis whitelist에 있는 유효한 refresh cookie가 필요하다.
- refresh token을 rotation하고 이전 token은 무효화한다.

### 로그아웃

`POST /api/v1/auth/logout`

규칙:

- Redis에서 refresh credential을 삭제한다.
- refresh cookie를 제거한다.

## 사용자 API

### 내 프로필 조회

`GET /api/v1/users/me`

응답 데이터:

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "tarouser",
  "status": "ACTIVE",
  "createdAt": "2026-05-19T10:00:00Z"
}
```

### 회원 탈퇴 요청

`POST /api/v1/users/me/withdrawal`

규칙:

- 인증된 사용자만 요청할 수 있다.
- 탈퇴 상태가 시작된 사용자는 새 상담을 만들 수 없다.

## 카드 API

### 타로 카드 목록 조회

`GET /api/v1/tarot/cards`

응답 데이터:

```json
{
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
}
```

규칙:

- 이 엔드포인트는 카드 도감 또는 완료된 리딩 표시용이다.
- draft 선택 화면에서는 카드 정체를 노출하는 용도로 사용하지 않는다.

## 상담 API

### Draft Deck 생성

`POST /api/v1/consultations/draft`

헤더:

```http
Authorization: Bearer <access_token>
```

요청:

```json
{
  "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요."
}
```

응답 데이터:

```json
{
  "draftId": "drf_abc123",
  "deckSize": 22,
  "expiresAt": "2026-05-19T10:40:00Z"
}
```

규칙:

- concern은 10-1000자이고 의미 있는 내용이어야 한다.
- 응답에는 실제 card ID를 포함하지 않는다.
- draft는 10분 후 만료된다.

### 상담 생성

`POST /api/v1/consultations`

헤더:

```http
Authorization: Bearer <access_token>
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

요청:

```json
{
  "draftId": "drf_abc123",
  "selections": [
    { "deckIndex": 4, "positionCode": "PRESENT" },
    { "deckIndex": 11, "positionCode": "OBSTACLE" },
    { "deckIndex": 17, "positionCode": "ADVICE" }
  ]
}
```

응답 데이터:

```json
{
  "consultationId": 1001,
  "status": "PENDING",
  "streamUrl": "/api/v1/consultations/1001/events"
}
```

규칙:

- 같은 사용자와 같은 idempotency key는 같은 consultationId를 반환한다.
- 만료, 누락, 사용 완료, 다른 사용자 소유 draft는 거부한다.
- 카드 정체는 이 요청이 수락된 뒤에만 공개한다.

### 상담 이벤트 구독

`GET /api/v1/consultations/{consultationId}/events`

헤더:

```http
Authorization: Bearer <access_token>
```

SSE 이벤트 순서:

```text
event: meta
data: {"consultationId":1001,"cards":[{"cardId":6,"cardName":"The Lovers","positionCode":"PRESENT"}]}

event: token
data: {"text":"이번 리딩은"}

event: done
data: {"result":{"summary":"관계에 대한 중요한 선택의 시기입니다.","overall":"...","cards":[],"advice":"...","caution":"..."}}
```

오류 이벤트:

```text
event: error
data: {"code":"AI_GENERATION_FAILED","message":"해석 생성에 실패했습니다. 잠시 후 다시 시도해주세요."}
```

규칙:

- 소유자만 구독할 수 있다.
- `meta`는 생성 텍스트보다 먼저 전송해야 한다.
- 연결이 끊겨도 가능한 경우 서버는 결과 생성과 저장을 완료한다.

### 상담 기록 목록 조회

`GET /api/v1/consultations?cursor=&size=20`

응답 데이터:

```json
{
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
}
```

규칙:

- 소유자만 조회할 수 있다.
- 최신순으로 반환한다.
- soft delete된 상담은 제외한다.

### 상담 상세 조회

`GET /api/v1/consultations/{consultationId}`

응답 데이터:

```json
{
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
}
```

규칙:

- 소유자만 조회할 수 있다.

### 상담 기록 삭제

`DELETE /api/v1/consultations/{consultationId}`

규칙:

- 소유자만 삭제할 수 있다.
- 실제 삭제가 아니라 soft delete로 처리한다.

## AI 서버 내부 API

백엔드는 신뢰된 서비스 네트워크에서 AI 서버를 호출한다.

### 해석 생성

`POST /internal/v1/interpretations/stream`

요청:

```json
{
  "requestId": "req-abc-123",
  "consultationId": 1001,
  "userId": 1,
  "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
  "spreadType": "THREE_CARD",
  "cards": [
    {
      "cardId": 6,
      "cardName": "The Lovers",
      "positionCode": "PRESENT",
      "orientation": "UPRIGHT"
    }
  ],
  "locale": "ko"
}
```

최종 페이로드:

```json
{
  "requestId": "req-abc-123",
  "status": "success",
  "category": "love",
  "modelName": "gpt-4o-mini",
  "modelProvider": "openai",
  "promptVersion": "tarot-v1.0",
  "documentVersion": "tarot-doc-v1.0",
  "retrievedDocIds": ["card-lovers-present-v1", "ctx-love-decision-001"],
  "result": {
    "summary": "관계에 대한 중요한 선택의 시기입니다.",
    "overall": "...",
    "cards": [],
    "advice": "...",
    "caution": "..."
  },
  "usage": {
    "inputTokens": 2500,
    "outputTokens": 900,
    "latencyMs": 6800
  }
}
```

규칙:

- 사용자 입력은 상담 고민으로만 취급하고 지시문으로 취급하지 않는다.
- 완료 전 출력은 result 스키마를 통과해야 한다.
- AI 로그에는 고민 원문 전체를 포함하지 않는다.
