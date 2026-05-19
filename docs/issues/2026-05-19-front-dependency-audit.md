# 프론트 의존성 감사 취약점

## 배경

제공 UI를 Next.js 화면으로 연결하고 빌드 검증을 하기 위해 `front/`에서 `npm.cmd install`을 실행했다.

## 확인 결과

`npm.cmd audit --audit-level=moderate` 결과 moderate 취약점 7건이 보고되었다.

- `esbuild`: 개발 서버 요청 노출 관련 advisory
  - 영향 경로: `vitest` -> `vite` -> `esbuild`
  - 현재 npm audit 기준 자동 수정 없음
- `postcss`: CSS stringify 출력 관련 XSS advisory
  - 영향 경로: `next` 내부 의존성
  - 현재 npm audit 기준 자동 수정 없음

## 영향

- 현재 보고된 항목은 직접 서비스 코드가 아니라 개발/프레임워크 하위 의존성에서 발생한다.
- `npm audit fix --force`는 주요 패키지 버전을 강제로 바꿀 수 있어 현재 spec의 기술 스택과 호환성을 깨뜨릴 수 있다.

## 임시 대응

- 자동 수정은 적용하지 않았다.
- `npm.cmd run build`는 성공했다.
- 이후 Next, Vite, Vitest 패치 버전에서 수정 가능 여부를 확인한 뒤 별도 작업으로 처리한다.

## 상태

- 미해결
- GitHub Issue 등록은 현재 권한 문제로 불가하므로 문서 이슈로 관리한다.
