# 프론트엔드 적용 명세

## 목적

이 문서는 `001-ai-tarot-consultation` 기능에서 프론트엔드 UI와 기능 연동 기준을 분리해 혼동을 줄이기 위한 보조 명세다. 제품 요구사항의 진실의 원천은 여전히 `spec.md`, `plan.md`, `contracts/api-contract.md`, `tasks.md`다.

## UI 적용 원칙

- 사용자가 제공한 UI 코드를 우선 참고한다.
- Codex는 제공 UI의 시각 방향을 유지하되, Next.js App Router, TypeScript, Tailwind CSS 구조에 맞게 다듬는다.
- 화면을 임의로 과도하게 확장하지 않는다.
- API 호출은 반드시 `front/src/api/` 하위 모듈을 통해 수행한다.
- 폼 검증은 `front/src/utils/validators.ts`에 둔다.
- Access Token은 기존 `front/src/stores/authStore.ts`와 `front/src/api/client.ts` 흐름을 따른다.

## 현재 반영된 참고 UI

사용자 제공 Stitch HTML을 다음 화면에 매핑했다.

- `_3/code.html`: 메인 오라클 화면 -> `front/app/page.tsx`
- `_2/code.html`: 회원가입 화면 -> `front/app/signup/page.tsx`
- `_4/code.html`: 로그인 화면 -> `front/app/login/page.tsx`

상담 입력 화면은 별도 HTML이 없으므로 `_3/code.html`의 오라클 입력 UI를 기반으로 `front/app/consultations/new/page.tsx`에 맞게 분리했다.

카드 선택 화면은 제공 HTML이 없으므로 같은 시각 언어를 유지한 최소 기능 화면으로 구성했다.

## 라우트 기준

- `/`: 메인 오라클 화면
- `/signup`: 회원가입
- `/login`: 로그인
- `/consultations/new`: 고민 입력 및 Draft Deck 생성
- `/consultations/select`: Draft Deck 기반 카드 3장 선택
- `/consultations/[id]/stream`: 상담 생성 이후 스트리밍 진입 화면

## 연동 기준

- 회원가입 성공 시 `/consultations/new`로 이동한다.
- 로그인 성공 시 `/consultations/new`로 이동한다.
- 메인 화면의 고민 입력은 `/consultations/new?concern=...`으로 전달한다.
- 고민 입력 제출은 `createDraft()`를 호출한다.
- Draft 생성 성공 시 `draftId`, `deckSize`, `expiresAt`을 쿼리로 전달해 `/consultations/select`로 이동한다.
- 카드 선택은 실제 카드 ID를 노출하지 않고 `deckIndex`만 관리한다.
- 카드 3장 선택 후 `createConsultation()`을 호출한다.
- 상담 생성 성공 시 `/consultations/{consultationId}/stream`으로 이동한다.

## 아직 남은 영역

- SSE 실제 수신 및 결과 렌더링은 백엔드 SSE 구현 이후 진행한다.
- 상담 기록 목록/상세 UI는 관련 API 구현 후 별도 제공 UI 또는 현재 시각 언어에 맞춰 연결한다.
- 프론트 E2E 테스트는 의존성 설치 및 실행 환경 정리 후 작성한다.
