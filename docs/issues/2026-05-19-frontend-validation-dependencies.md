# 프론트엔드 검증 의존성 미설치

## 상태

- 발견일: 2026-05-19
- 상태: 미해결
- 관련 작업: `T029`, `T030`, 이후 `T022`

## 문제

프론트 인증 API 모듈과 인증 상태 저장소 구현 후 테스트를 실행하려 했지만, `front/node_modules`가 없어 검증 명령이 실패했다.

실행 결과:

```text
npm.cmd run test -- --run
'vitest' is not recognized as an internal or external command
```

## 영향

- 현재 프론트 TypeScript/Vitest/Playwright 검증을 로컬에서 실행할 수 없다.
- `T022` 프론트 인증 E2E 테스트 작성 및 실행 전 의존성 설치가 필요하다.

## 처리 방안

사용자 확인 후 `front` 디렉터리에서 패키지 설치를 진행한다.

```bash
npm install
```

설치 후 다음 명령으로 검증한다.

```bash
npm.cmd run test -- --run
npm.cmd run test:e2e
```

## 메모

프로젝트 규칙상 `npm install` 같은 패키지/빌드 도구 변경은 사용자에게 먼저 알리고 동의를 받은 뒤 진행한다.
