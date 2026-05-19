# 데이터 모델: AI 타로 상담 서비스 MVP

## 엔티티: User

등록된 서비스 회원을 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| email | string | 필수, 고유, 이메일 형식 |
| passwordHash | string | 필수, bcrypt hash만 저장 |
| nickname | string | 필수, 2-20자 |
| termsAgreedAt | datetime | 필수 |
| privacyAgreedAt | datetime | 필수 |
| marketingAgreedAt | datetime | 선택 |
| status | enum | ACTIVE, WITHDRAWAL_REQUESTED, WITHDRAWN |
| createdAt | datetime | 필수 |
| updatedAt | datetime | 필수 |
| deletedAt | datetime | 선택 |

관계:

- User는 여러 Consultation을 가진다.
- User는 Redis에 저장된 DraftDeck의 소유자다.

검증:

- 탈퇴 요청 또는 삭제된 사용자는 새 상담을 생성할 수 없다.
- 이메일 고유성은 활성 계정과 보관 계정 범위에서 보장한다.

## 엔티티: TarotCard

표시 가능한 타로 카드를 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| nameEn | string | 필수 |
| nameKo | string | 필수 |
| arcana | enum | MVP는 MAJOR 사용 |
| suit | string | 메이저 아르카나는 null |
| cardNumber | number | 메이저 아르카나 0-21 |
| imageUrl | string | 선택 |
| createdAt | datetime | 필수 |

검증:

- MVP seed는 메이저 아르카나 22장만 포함한다.
- draft 선택 단계에서는 카드 정체를 노출하지 않는다.

## 엔티티: CardInterpretation

특정 카드 맥락에 대한 큐레이션된 의미 문서를 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| cardId | number | 필수 TarotCard 참조 |
| orientation | enum | MVP는 UPRIGHT 사용 |
| positionCode | enum | PRESENT, OBSTACLE, ADVICE |
| keywords | array | 필수 keyword 목록 |
| interpretation | text | 필수 큐레이션 콘텐츠 |
| documentVersion | string | 필수 |
| isActive | boolean | 필수 |
| createdAt | datetime | 필수 |

검증:

- 활성 문서 조회는 card, orientation, position 기준 최신 documentVersion을 선택한다.
- MVP 최소 seed는 22장 x 3개 위치 = 66건이다.

## 임시 모델: DraftDeck

Redis에 저장되는 서버 셔플 카드 매핑을 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| draftId | string | 필수 opaque identifier |
| userId | number | 필수 소유자 |
| concern | text | 필수, 10-1000자 |
| deckMapping | array | 필수 셔플 card ID 목록, 클라이언트에는 미노출 |
| expiresAt | datetime | 필수, 10분 TTL |
| used | boolean | 필수 |

검증:

- 소유자만 draft를 사용할 수 있다.
- 만료, 누락, 사용 완료 draft는 사용할 수 없다.
- 클라이언트에는 draftId, deckSize, expiresAt만 반환한다.

## 값 객체: CardSelection

리딩 위치에 배정된 draft index 선택을 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| deckIndex | number | 필수, 0-21 |
| positionCode | enum | PRESENT, OBSTACLE, ADVICE |
| positionOrder | number | positionCode에서 파생된 1-3 값 |

검증:

- 정확히 3개 selection이 필요하다.
- deckIndex는 서로 달라야 한다.
- positionCode는 서로 달라야 하며 세 위치를 모두 포함해야 한다.

## 엔티티: Consultation

저장된 타로 상담 요청과 결과를 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| userId | number | 필수 소유자 |
| concern | text | 필수, 민감 정보 |
| spreadType | enum | THREE_CARD |
| categoryCode | enum | love, career, relationship, money, health, other |
| status | enum | PENDING, STREAMING, COMPLETED, FAILED, CANCELLED |
| resultSummary | string | 완료 전까지 선택 |
| resultDetail | object | 완료 시 필수 |
| retrievedDocIds | array | 선택 |
| modelName | string | 선택 |
| modelProvider | string | 선택 |
| promptVersion | string | 선택 |
| documentVersion | string | 선택 |
| idempotencyKey | string | 사용자 범위 필수 |
| createdAt | datetime | 필수 |
| updatedAt | datetime | 필수 |
| completedAt | datetime | 선택 |
| deletedAt | datetime | 선택 soft delete 표시 |

관계:

- Consultation은 User에 속한다.
- Consultation은 생성 후 정확히 세 개의 ConsultationCard를 가진다.
- Consultation은 여러 AIRequestLog를 가질 수 있다.

상태 전이:

```text
PENDING -> STREAMING -> COMPLETED
PENDING -> STREAMING -> FAILED
PENDING -> CANCELLED
STREAMING -> CANCELLED
```

검증:

- 소유자만 조회, 스트리밍, 삭제할 수 있다.
- 삭제된 상담은 기본 기록 목록에서 제외한다.
- 완료된 상담은 필수 구조화 결과 필드를 가져야 한다.

## 엔티티: ConsultationCard

draft가 수락된 뒤 실제로 선택된 카드를 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| consultationId | number | 필수 Consultation 참조 |
| cardId | number | 필수 TarotCard 참조 |
| positionOrder | number | 필수, 상담 내 고유 |
| positionCode | enum | 필수, 상담 내 고유 |
| orientation | enum | MVP는 UPRIGHT 사용 |
| createdAt | datetime | 필수 |

검증:

- 각 상담은 PRESENT, OBSTACLE, ADVICE 카드를 각각 하나씩 가진다.

## 엔티티: ContextDocument

해석 보강에 선택적으로 사용하는 일반 조언 문서다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| categoryCode | enum | love, career, relationship, money, health, other |
| title | string | 필수 |
| content | text | 필수 |
| embedding | object | 선택 |
| documentVersion | string | 필수 |
| isActive | boolean | 필수 |
| createdAt | datetime | 필수 |

검증:

- 컨텍스트 검색 실패는 MVP 해석 생성을 막지 않는다.

## 엔티티: AIRequestLog

AI 생성에 대한 비민감 운영 metadata를 나타낸다.

| 필드 | 타입 | 규칙 |
|---|---|---|
| id | number | 기본 식별자 |
| consultationId | number | 선택 Consultation 참조 |
| requestId | string | 필수 |
| inputTokens | number | 선택 |
| outputTokens | number | 선택 |
| latencyMs | number | 선택 |
| status | enum | SUCCESS, FAILED, RETRIED, FALLBACK |
| errorCategory | string | 선택 |
| errorMessage | text | 선택, sanitize된 message만 저장 |
| createdAt | datetime | 필수 |

검증:

- 고민 원문 전체, 비밀번호, JWT, refresh token은 기록하지 않는다.
