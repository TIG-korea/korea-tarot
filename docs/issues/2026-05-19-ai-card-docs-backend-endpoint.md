# AI 카드 해석 문서 조회 백엔드 내부 API 필요

## 상태

- 등록일: 2026-05-19
- 유형: 후속 구현 이슈
- 관련 작업: `T054`, `T057`

## 내용

AI 서버에 카드 해석 문서 조회 클라이언트는 추가했지만, 실제 카드 해석 문서를 제공할 백엔드 내부 API가 아직 구현되어 있지 않다.

현재 AI 서버 클라이언트는 다음 경로를 호출하도록 구성되어 있다.

```text
POST /internal/v1/card-interpretations/lookup
```

요청은 선택 카드 3장의 `cardId`, `positionCode`, `orientation`을 보내고, 응답은 각 위치별 활성 카드 해석 문서 3건을 반환하는 구조를 기대한다.

## 필요한 조치

1. 백엔드에 내부 카드 해석 문서 조회 엔드포인트를 추가한다.
2. `card_interpretations`에서 `card_id`, `orientation`, `position_code`, `is_active` 기준으로 최신 문서를 조회한다.
3. AI 서버가 기대하는 `documents` 응답 구조와 문서 ID, 키워드, 해석, 문서 버전을 맞춘다.
4. 백엔드 내부 API 계약을 spec 또는 contract에 반영할지 결정한다.

## 임시 영향

- AI 서버 단위 테스트는 `httpx.MockTransport`로 검증 가능하다.
- 실제 스트리밍 해석 엔드포인트 연동 시점에는 이 내부 API가 없으면 카드 문서 조회 단계에서 실패한다.
# 처리 상태

- 상태: 해결됨
- 해결일: 2026-05-19
- 처리 내용: `POST /internal/v1/card-interpretations/lookup` 내부 API를 추가해 AI 서버가 선택 카드 3장의 활성 카드 해석 문서를 조회할 수 있게 했다.
- 응답 구조: `ApiResponse.data.documents` 안에 `documentId`, `cardId`, `cardName`, `positionCode`, `orientation`, `keywords`, `interpretation`, `documentVersion`을 포함한다.
