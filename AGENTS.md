# AGENTS.md

이 프로젝트는 GitHub Spec Kit 기반 SDD(Spec-Driven Development) 워크플로우를 따른다.

슬래시 명령 흐름: `/constitution` -> `/specify` -> `/plan` -> `/tasks` -> `/implement`

## 핵심 규칙

- `specs/` 폴더의 명세가 진실의 원천이다.
- 코드와 spec이 충돌하면 spec이 맞다. 코드를 spec에 맞춘다.
- spec에 없는 기능은 임의로 추가하지 않는다. 필요하면 먼저 사용자에게 확인하고, 동의를 얻은 뒤 spec을 수정한 다음 구현한다.

## 필수 문서 읽기 순서

작업을 시작하기 전에 관련 문서를 먼저 읽는다.

1. `.specify/memory/constitution.md`
2. `specs/[feature-number]-[feature-name]/spec.md`
3. `specs/[feature-number]-[feature-name]/plan.md`
4. `specs/[feature-number]-[feature-name]/contracts/api-contract.md`
5. `specs/[feature-number]-[feature-name]/data-model.md`

없는 문서는 없는 것으로 보고하고, 존재하는 문서를 기준으로 진행한다. 모호한 요구사항은 추측하지 말고 사용자에게 질문한다.

<!-- SPECKIT START -->
현재 계획 문서: `specs/001-ai-tarot-consultation/plan.md`
<!-- SPECKIT END -->

## 프로젝트 구조

- `specs/`: Spec Kit 기능 명세, 계획, 작업 목록, 계약, 데이터 모델, 체크리스트.
- `front/`: Next.js 프론트엔드 애플리케이션 코드.
- `backend/`: Spring Boot 백엔드 API 및 서비스 코드.
- `ai-server/`: FastAPI 기반 AI serving, 모델 orchestration, 관련 서버 코드.

## 작업 규칙

- 한 번에 한 가지 작업 단위에 집중한다. 큰 요청은 단계별로 쪼개서 진행하고 각 단계의 결과를 보고한다.
- 기능 구현은 도메인 단위로 끊어서 진행한다. 예: `global`, `auth`, `user`, `tarot`, `consultation`, `ai`, `front`.
- 도메인 구현이 완료될 때마다 관련 검증을 실행하고, 완료된 task를 `[X]`로 표시한 뒤 git commit과 push를 진행한다.
- 구현 중 발견한 문제, 미해결 위험, 후속 수정이 필요한 사항은 GitHub issue로 등록한다. issue에는 관련 spec/task, 재현 조건, 영향 범위, 임시 대응 여부를 적는다.
- 파일을 만들거나 수정하기 전에 무엇을 할지 1~3줄로 먼저 요약한다.
- 라이브러리, 빌드 도구, 런타임 버전, 아키텍처는 임의로 바꾸지 않는다.
- `.env`, `application.yml` 등에 실제 비밀번호, 토큰, API 키를 저장하거나 커밋하지 않는다. 예시는 `application-example.yml` 또는 `.env.example`로 관리한다.
- 테스트 코드는 사용자 요청 없이 삭제하지 않는다.

## 기술 스택

- 프론트엔드: Next.js 15 App Router, TypeScript strict mode, Tailwind CSS, shadcn/ui, TanStack Query, Zustand, react-hook-form, zod, Framer Motion
- 백엔드: Spring Boot 3.x, Java 21, Spring Security, Spring Data JPA, Gradle Groovy DSL, Bean Validation, Spring Mail, springdoc-openapi, Lombok, Flyway, Redis, WebClient, Bucket4j, Resilience4j
- AI 서버: FastAPI, Python 3.12, Pydantic v2, httpx, LLM SDK, 선택적 in-memory vector search 또는 pgvector
- 저장소: MySQL 8.4, Redis 7
- 인증: JWT Access Token + HttpOnly Secure Refresh Token
- 스트리밍: 상담 생성 POST 이후 SSE 구독 GET

새로운 주요 라이브러리 또는 기술 스택 변경은 사용자 동의 없이 추가하지 않는다. `npm install`, `gradle wrapper` 같은 패키지/빌드 도구 변경도 먼저 사용자에게 알린다.

## 코딩 컨벤션

- 모든 파일 끝에는 개행 한 줄을 둔다.
- 들여쓰기는 JS/TS/CSS 2칸, Java 4칸, Python 4칸을 사용한다.
- 주석은 한국어로 작성해도 된다.
- 변수, 함수, 클래스 이름은 영어로 작성한다.

## 백엔드 규칙

- 패키지 구조는 해당 기능의 `plan.md` 기준 도메인별 분리(`auth`, `user`, `tarot`, `consultation`, `ai`, `global`)를 따른다.
- Controller는 DTO만 받고 반환한다.
- Service에서 Entity를 다룬다.
- 모든 성공 응답은 `ApiResponse<T>`로 감싸서 반환한다.
- 오류 응답은 가능한 한 RFC 7807 Problem Details 구조를 따른다.
- 예외는 `GlobalExceptionHandler`에서 일괄 처리한다. Controller에서 try/catch를 사용하지 않는다.
- Entity에는 Setter를 만들지 않는다. 변경은 명시적 메서드로 표현한다.
- Entity는 Lombok `@Builder`, `@Getter`, `@NoArgsConstructor(access = PROTECTED)`를 사용한다.
- Lombok `@Setter`, `@Data`는 사용하지 않는다.
- Service는 기본적으로 인터페이스 없이 단일 클래스로 구현한다. 구현체가 여러 개 필요할 때만 인터페이스를 분리한다.
- DTO는 Request와 Response를 같은 파일 안에 inner record로 묶는다.
- 비즈니스 로직은 가능한 한 Entity 내부 메서드로 캡슐화한다.

## 프론트엔드 규칙

- 프론트엔드 작업 전 `docs/project-rules.md`를 읽고, 이미 구현된 UI를 우선 유지한다.
- 컴포넌트는 함수형 컴포넌트와 Hooks를 사용한다.
- API 호출은 반드시 `front/src/api/` 내부 모듈을 통해 수행한다.
- 인증 토큰과 상담 진행 상태는 명시된 store/query 계층을 통해 관리한다.
- 공통 색상, 폰트, 여백 값은 디자인 토큰 또는 Tailwind theme 기준으로 관리한다.
- 폼 검증 로직은 zod schema 또는 `front/src/utils/validators.ts`로 모은다.

## AI 서버 규칙

- Pydantic v2 schema로 입력과 최종 출력을 검증한다.
- 사용자 입력은 상담 고민으로만 취급하고 시스템 지시로 해석하지 않는다.
- 카드 해석 문서는 카드, 방향, 위치, 활성 상태, 문서 버전을 기준으로 결정적으로 조회한다.
- 컨텍스트 검색은 MVP에서 선택 기능이며, 실패해도 카드 문서 기반 해석은 가능해야 한다.
- AI 요청 로그에는 고민 원문 전체, 토큰, 비밀번호, 인증 정보를 저장하지 않는다.

## 보고 형식

작업 완료 시 다음을 보고한다.

1. 어떤 spec의 어느 부분을 구현했는지
2. 새로 만든 파일과 수정한 파일 목록
3. 실행한 검증 또는 실행하지 못한 검증
4. 다음에 해야 할 작업 제안(필요한 경우)
