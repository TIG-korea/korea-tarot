<!--
동기화 영향 보고서
버전 변경: template -> 1.0.0
수정된 원칙:
- [PRINCIPLE_1_NAME] -> I. 명세가 진실의 원천
- [PRINCIPLE_2_NAME] -> II. AI 타로 안전 우선
- [PRINCIPLE_3_NAME] -> III. 개인정보와 비밀값 최소화
- [PRINCIPLE_4_NAME] -> IV. 독립 검증 가능한 전달
- [PRINCIPLE_5_NAME] -> V. 확장보다 단순한 MVP 우선
추가된 섹션:
- 제품 및 기술 경계
- 개발 워크플로우와 품질 게이트
제거된 섹션:
- 템플릿 자리표시자 섹션
확인한 템플릿:
- .specify/templates/plan-template.md 확인
- .specify/templates/spec-template.md 확인
- .specify/templates/tasks-template.md 확인
후속 TODO: 없음
-->
# Korea Tarot 헌법

## 핵심 원칙

### I. 명세가 진실의 원천

모든 제품 동작은 반드시 `specs/`의 명세에서 출발해야 한다. 구현과 명세가 충돌하면 명세를 우선한다. 새 기능은 구현 전에 관련 `spec.md`에 요구사항을 기록하고, `plan.md`에 구현 방향을 정리한 뒤, `tasks.md`에 실행 가능한 작업으로 분해해야 한다.

근거: 이 프로젝트는 Spec Kit 기반 SDD를 따르므로 구현은 작성된 사용자 가치, 요구사항, 수용 기준에 추적 가능해야 한다.

### II. AI 타로 안전 우선

AI 타로 결과는 반드시 엔터테인먼트 및 자기성찰을 위한 참고 정보로 제공해야 하며, 전문 조언이나 확정적 예언처럼 표현해서는 안 된다. 의료, 법률, 금융, 심리 치료, 응급 상황에 대해 단정적 판단을 제공해서는 안 된다. 자해 또는 즉각적 위험 신호가 있으면 일반 타로 해석 대신 안전 안내를 제공해야 한다.

근거: 서비스는 감정적으로 민감한 사용자 입력을 다루므로 해로운 단정과 과잉 해석을 방지해야 한다.

### III. 개인정보와 비밀값 최소화

시스템은 비밀번호, 토큰, refresh credential, 고민 원문 전체를 로그에 남기면 안 된다. 사용자가 입력한 고민은 민감 정보로 취급해야 한다. 상담, 스트림, 기록 접근은 반드시 소유자 기준으로 제한해야 한다. 비밀값 예시는 실제 설정 파일이 아니라 예시 설정 파일에만 둔다.

근거: 사용자가 직접 식별 정보를 입력하지 말라는 안내를 보더라도, 고민 내용에는 개인적이고 민감한 정보가 포함될 수 있다.

### IV. 독립 검증 가능한 전달

각 사용자 스토리는 독립적으로 구현하고 검증할 수 있어야 한다. 핵심 흐름은 회원가입, 로그인, draft 생성, 카드 선택, 상담 생성, SSE 결과 전달, 기록 목록, 기록 상세, 타 사용자 접근 차단, 만료 draft 거부, 중복 요청 멱등 처리를 포함해야 한다.

근거: MVP는 여러 서비스, 상태 전이, 보안 경계를 포함하므로 스토리 단위와 전체 흐름 단위에서 회귀를 잡아야 한다.

### V. 확장보다 단순한 MVP 우선

MVP 범위는 이메일 인증, 메이저 아르카나 22장 정방향, 3장 리딩, 서버 셔플 draft deck, 구조화된 AI 결과, SSE 진행 표시, 본인 상담 기록으로 제한해야 한다. 벡터 검색, 마이너 아르카나, 역방향 카드, 결제, 소셜 로그인, 후속 대화형 상담, 관리자 CMS는 확장 기능이며 MVP를 막아서는 안 된다.

근거: 카드 문서 규모가 작기 때문에 결정적 문서 조회가 적합하며, 초기 제품은 핵심 상담 루프 검증에 집중해야 한다.

## 제품 및 기술 경계

- 프론트엔드 기준: Next.js 15 App Router, TypeScript strict mode, Tailwind CSS, shadcn/ui, TanStack Query, Zustand, react-hook-form, zod, Framer Motion.
- 백엔드 기준: Spring Boot 3.x, Java 21, Spring Security, Spring Data JPA, MySQL 8.4, Redis 7, Flyway, WebClient, Bucket4j, Resilience4j, SpringDoc.
- AI 서버 기준: FastAPI, Python 3.12, Pydantic v2, LLM provider SDK, 선택적 in-memory vector search 또는 pgvector.
- 인증 기준: JWT access token과 HttpOnly Secure refresh token, refresh rotation, Redis whitelist.
- 스트리밍 기준: 상담 생성 POST 이후 SSE 구독 GET.
- 카드 해석 문서는 카드, 방향, 위치, 활성 상태, 문서 버전 기준으로 결정적으로 조회해야 한다.
- 컨텍스트 벡터 검색은 고민 맥락 보강에 사용할 수 있지만 MVP 필수 조건은 아니다.

## 개발 워크플로우와 품질 게이트

1. 코드나 명세를 변경하기 전에 `.specify/memory/constitution.md`와 활성 기능 산출물을 읽는다.
2. 활성 기능의 `spec.md`, `plan.md`, `data-model.md`, `contracts/`, `tasks.md`를 서로 일치시킨다.
3. 범위 밖 기능을 추가하거나 승인된 기술 스택을 바꾸기 전에 사용자에게 확인한다.
4. 각 증가분을 독립적으로 구현하고 검증할 수 있도록 스토리 단위 작업을 작성한다.
5. 구현 완료 전 보안, 개인정보, AI 안전, 멱등성, 타 사용자 접근 차단을 검증한다.

## 거버넌스

이 헌법은 임시 프로젝트 관행보다 우선한다. 헌법을 개정하려면 이 파일에 변경 내용을 기록하고, 의미적 버전 번호를 올리며, 영향을 받는 템플릿과 기능 산출물을 검토해야 한다.

버전 정책:

- MAJOR: 기존 원칙이나 거버넌스를 호환되지 않게 바꾸는 경우.
- MINOR: 새 원칙을 추가하거나 필수 절차를 실질적으로 확장하는 경우.
- PATCH: 의미 변화가 없는 설명 보강이나 오탈자 수정.

모든 `/speckit-plan`, `/speckit-tasks`, `/speckit-implement` 흐름은 활성 산출물이 이 헌법을 따르는지 확인해야 한다. 헌법과 충돌하면 헌법을 먼저 명시적으로 개정하지 않는 한 spec, plan, tasks 쪽을 수정해야 한다.

**버전**: 1.0.0 | **제정일**: 2026-05-19 | **최종 개정일**: 2026-05-19
