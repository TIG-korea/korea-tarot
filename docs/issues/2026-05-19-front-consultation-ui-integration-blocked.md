# 프론트 상담 UI 연동 대상 파일 부재

## 배경

Spec Kit 작업 목록 기준 다음 작업은 `T042` 고민 입력 화면과 `T043` 카드 선택 화면이다.

- `T042 [US2] 고민 입력 화면을 구현한다: front/app/consultations/new/page.tsx`
- `T043 [US2] 카드 선택 화면을 구현한다: front/app/consultations/select/page.tsx`

다만 프로젝트 규칙상 프론트 UI는 사용자가 이미 구현한 화면을 우선 사용하며, Codex는 UI를 임의 생성하지 않고 기능 연동만 수행해야 한다.

## 확인 결과

현재 저장소의 `front/app` 아래에는 다음 파일만 존재한다.

- `front/app/layout.tsx`
- `front/app/page.tsx`

따라서 `front/app/consultations/new/page.tsx`와 `front/app/consultations/select/page.tsx`가 없어 기존 UI에 `front/src/api/consultations.ts`의 `createDraft` 기능을 연결할 수 없다.

## 영향

- `T042`, `T043`은 현재 상태에서 진행할 수 없다.
- UI를 새로 만들면 사용자 지시인 "UI 생성은 하지 않고 기능만 합친다"는 기준을 위반할 수 있다.
- 상담 Draft API 모듈은 이미 준비되어 있으므로, 화면 파일이 추가되면 API 연동부터 이어서 진행할 수 있다.

## 제안 해결

1. 사용자가 구현한 상담 입력/카드 선택 UI 파일을 `front/app` 또는 실제 사용 경로에 반영한다.
2. 반영 후 Codex가 기존 UI 구조를 읽고 다음 기능만 연결한다.
   - 고민 입력 제출 시 `createDraft` 호출
   - `draftId`, `deckSize`, `expiresAt` 상태 전달
   - 카드 선택 화면으로 이동
   - 카드 선택 화면에서 실제 카드 ID 노출 없이 `deckIndex` 3개만 관리
3. 그 후 `T042`, `T043` 완료 여부를 다시 판단한다.

## 상태

- 미해결
- GitHub Issue 등록은 현재 권한 문제로 불가하므로 문서 이슈로 관리한다.
