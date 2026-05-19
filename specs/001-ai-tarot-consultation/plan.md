# 구현 계획: AI 타로 상담 서비스 MVP

**브랜치**: `001-ai-tarot-consultation` | **날짜**: 2026-05-19 | **명세**: [spec.md](./spec.md)  
**입력**: `specs/001-ai-tarot-consultation/spec.md`의 기능 명세

## 요약

인증된 사용자가 고민을 입력하고, 서버가 셔플한 draft deck에서 카드 3장을 선택한 뒤, AI 해석을 스트리밍으로 받고, 이후 저장된 상담 기록을 다시 조회할 수 있는 웹 서비스를 구현한다. 구조는 Next.js 프론트엔드, Spring Boot 백엔드, MySQL/Redis 저장소, FastAPI AI 서버로 분리한다. 카드 해석 문서는 결정적으로 조회하고, 고민 맥락 보강 검색은 선택 기능으로 둔다.

## 기술 맥락

**언어/버전**: TypeScript strict mode, Java 21, Python 3.12  
**주요 의존성**: Next.js 15 App Router, Tailwind CSS, shadcn/ui, TanStack Query, Zustand, react-hook-form, zod, Framer Motion, Spring Boot 3.x, Spring Security, Spring Data JPA, Flyway, Redis 클라이언트, WebClient, Bucket4j, Resilience4j, springdoc-openapi, Lombok, FastAPI, Pydantic v2, httpx, LLM SDK  
**저장소**: 영속 데이터는 MySQL 8.4, refresh credential/draft deck/idempotency/rate limit은 Redis 7, 컨텍스트 문서 보강에는 선택적으로 pgvector 또는 in-memory vector search 사용  
**테스트**: JUnit 5, Testcontainers, REST Assured, pytest, pytest-asyncio, Vitest, Playwright, k6, LLM golden set evaluation  
**대상 플랫폼**: 웹 브라우저 클라이언트와 Linux 호환 API 서비스  
**프로젝트 유형**: 프론트엔드, 백엔드, AI 서버를 포함한 모노레포 웹 애플리케이션  
**성능 목표**: 정상 조건에서 p95 첫 진행 표시 3초 이내, p95 전체 성공 해석 12초 이내  
**제약**: 고민 원문 전체, 비밀번호, JWT, refresh token은 로그에 남기지 않는다. 상담 생성 전에는 서버 셔플 카드 정체를 숨긴다. 상담과 스트림은 소유자 기준으로 접근 제어한다.  
**규모/범위**: 일반 사용자와 재방문 사용자 대상 MVP. 메이저 아르카나 22장 정방향, 정확히 3개 위치만 포함한다. 결제, 소셜 로그인, 후속 대화형 상담, 마이너 아르카나, 역방향 카드, 관리자 CMS는 제외한다.

## 헌법 점검

*게이트: Phase 0 조사 전 통과해야 하며, Phase 1 설계 후 다시 확인한다.*

- **명세가 진실의 원천**: 통과. 이 계획은 활성 spec을 구현하며 API, 데이터 모델, 작업 목록을 같은 기능 디렉터리에 유지한다.
- **AI 타로 안전 우선**: 통과. AI 안전 정책, 고지 문구, 민감 주제 처리를 spec, 데이터 모델, 계약, 작업에 포함했다.
- **개인정보와 비밀값 최소화**: 통과. 로그 제한, 소유자 접근 제어, 민감 데이터 제약이 명시되어 있다.
- **독립 검증 가능한 전달**: 통과. 작업은 사용자 스토리별로 묶고 테스트 기준과 E2E 검증을 포함한다.
- **확장보다 단순한 MVP 우선**: 통과. 벡터 검색은 선택 기능이며 Phase 2 기능은 MVP 작업에서 제외한다.

## 프로젝트 구조

### 문서 구조

```text
specs/001-ai-tarot-consultation/
  plan.md
  research.md
  data-model.md
  quickstart.md
  contracts/
    api-contract.md
  tasks.md
```

### 소스 코드 구조

```text
front/
  app/
  src/
    api/
    components/
    features/
    stores/
    styles/
    utils/
  tests/

backend/
  build.gradle
  src/main/java/com/koreatarot/
    auth/
    user/
    tarot/
    consultation/
    ai/
    global/
  src/main/resources/db/migration/
  src/test/java/com/koreatarot/

ai-server/
  app/
    api/
    core/
    schemas/
    services/
    prompts/
  tests/

docker-compose.yml
```

**구조 결정**: 제품 책임과 런타임 관심사를 분리하기 위해 `front/`, `backend/`, `ai-server/` 3개 앱을 유지한다.

## Phase 0: 조사 결정

[research.md](./research.md)에 정리한다.

## Phase 1: 설계 산출물

- [data-model.md](./data-model.md): 영속 엔티티, 임시 draft 모델, 검증 규칙, 상태 전이.
- [contracts/api-contract.md](./contracts/api-contract.md): 인증, 카드, 상담, 기록, 사용자 프로필, AI 서버 연동을 위한 REST 및 SSE API 계약.
- [quickstart.md](./quickstart.md): 로컬 실행과 검증 시나리오.

## 백엔드 설계

- Controller는 DTO만 받고 반환한다.
- 도메인 Service는 비즈니스 흐름과 상태 전이를 담당한다.
- JPA Entity는 setter 대신 명시적 동작 메서드를 사용한다.
- 성공 응답은 `ApiResponse<T>`로 감싼다. 오류 응답은 RFC 7807 Problem Details 구조를 따른다.
- `GlobalExceptionHandler`는 validation, authorization, draft, idempotency, AI, rate-limit 오류를 매핑한다.
- 상담 생성 시 `consultations`와 `consultation_cards`는 짧은 하나의 트랜잭션으로 저장한다. AI 생성과 결과 상태 업데이트는 별도 짧은 트랜잭션으로 처리한다.

## 프론트엔드 설계

- 화면은 메인, 회원가입, 로그인, 고민 입력, 카드 선택, 결과 스트리밍, 결과 상세, 기록 목록, 기록 상세, 마이페이지를 포함한다.
- API 호출은 `front/src/api/` 아래에 모은다.
- 클라이언트 상태는 인증 사용자, 메모리 access token, 고민, draft ID, 카드 선택, consultation ID, stream 상태, 누적 streaming text, 최종 결과를 관리한다.
- SSE는 상담 생성 POST 이후 GET 구독으로 처리한다. Authorization header가 필요하면 브라우저 기본 `EventSource` 대신 fetch 기반 SSE 클라이언트를 사용한다.

## AI 서버 설계

- Pydantic 스키마로 입력과 출력을 검증한다.
- 고민 카테고리를 분류한다.
- 카드 해석 문서를 결정적으로 조회한다.
- 카테고리 컨텍스트 문서는 선택적으로 검색한다.
- 시스템 메시지와 사용자 입력을 엄격히 분리해 prompt를 구성한다.
- 생성 텍스트를 스트리밍하면서 최종 JSON 구조 결과를 만든다.
- timeout 또는 schema 위반은 안전한 범위에서 1회 재시도한다.
- 로깅용 사용량 메타데이터는 비민감 정보만 반환한다.

## 보안 및 관찰성

- refresh credential은 Redis whitelist에 저장하고 refresh 시 rotation한다.
- draft deck TTL은 10분이다.
- idempotency는 사용자 ID와 key 조합으로 제한하고 최소 24시간 보관한다.
- 회원가입, 로그인 실패, 상담 생성, 동일 고민 반복 제출에 rate limit을 적용한다.
- 로그는 correlation ID, 상태 전이, AI latency, token count, error category를 포함하되 고민 원문 전체와 credential은 제외한다.
- Sentry, Langfuse, Prometheus, Grafana를 관찰성 대상으로 둔다.

## 복잡도 추적

| 위반 | 필요한 이유 | 더 단순한 대안을 거절한 이유 |
|------|-------------|------------------------------|
| 없음 | 해당 없음 | 해당 없음 |
