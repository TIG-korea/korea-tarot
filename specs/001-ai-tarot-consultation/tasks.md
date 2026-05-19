# 작업 목록: AI 타로 상담 서비스 MVP

**입력**: `specs/001-ai-tarot-consultation/`의 설계 문서  
**선행 문서**: plan.md, spec.md, research.md, data-model.md, contracts/api-contract.md

**테스트**: 헌법과 성공 기준에 따라 핵심 흐름 테스트는 필수다.

**구성 방식**: 각 사용자 스토리를 독립적으로 구현하고 테스트할 수 있도록 작업을 스토리별로 묶는다.

## Phase 1: 설정 (공통 인프라)

**목적**: 모노레포 런타임 구조와 로컬 인프라를 초기화한다.

- [X] T001 루트 Docker Compose에 MySQL과 Redis를 구성한다: docker-compose.yml
- [X] T002 백엔드 Spring Boot 프로젝트 골격을 만든다: backend/build.gradle, backend/settings.gradle, backend/src/main/java/com/koreatarot/KoreaTarotApplication.java
- [X] T003 프론트엔드 Next.js TypeScript 프로젝트 골격을 만든다: front/package.json, front/app/layout.tsx, front/app/page.tsx
- [X] T004 FastAPI 프로젝트 골격을 만든다: ai-server/requirements.txt, ai-server/app/main.py, ai-server/app/api/router.py
- [X] T005 [P] 환경변수 예시 파일을 만든다: backend/src/main/resources/application-example.yml, front/.env.example, ai-server/.env.example
- [X] T006 [P] 기본 GitHub Actions CI를 만든다: .github/workflows/ci.yml

---

## Phase 2: 기반 작업 (모든 스토리의 선행 조건)

**목적**: 공통 계약, 저장소, 보안, 관찰성 기본 요소를 준비한다.

**중요**: 이 단계가 끝나기 전에는 사용자 스토리 구현을 시작하지 않는다.

- [X] T007 백엔드 Gradle 의존성을 구성한다: backend/build.gradle
- [X] T008 백엔드 application profile을 구성한다: backend/src/main/resources/application.yml
- [X] T009 Flyway 기본 테이블 migration을 작성한다: backend/src/main/resources/db/migration/V1__create_users_table.sql, backend/src/main/resources/db/migration/V2__create_core_tables.sql
- [X] T010 메이저 아르카나와 카드 해석 seed migration을 작성한다: backend/src/main/resources/db/migration/V3__seed_major_arcana.sql, backend/src/main/resources/db/migration/V4__seed_card_interpretations.sql
- [X] T011 [P] ApiResponse 응답 래퍼를 구현한다: backend/src/main/java/com/koreatarot/global/api/ApiResponse.java
- [X] T012 [P] Problem Details 오류 모델을 구현한다: backend/src/main/java/com/koreatarot/global/error/ProblemDetailResponse.java
- [X] T013 GlobalExceptionHandler를 구현한다: backend/src/main/java/com/koreatarot/global/error/GlobalExceptionHandler.java
- [X] T014 correlation ID filter를 구현한다: backend/src/main/java/com/koreatarot/global/observability/CorrelationIdFilter.java
- [X] T015 Spring Security 기본 구성을 구현한다: backend/src/main/java/com/koreatarot/global/security/SecurityConfig.java
- [X] T016 Redis 구성을 구현한다: backend/src/main/java/com/koreatarot/global/redis/RedisConfig.java
- [X] T017 [P] 프론트엔드 API 클라이언트 기반을 구성한다: front/src/api/client.ts
- [X] T018 [P] 프론트엔드 app layout과 global style을 구성한다: front/app/layout.tsx, front/src/styles/globals.css
- [X] T019 [P] AI 서버 설정과 logging을 구성한다: ai-server/app/core/config.py, ai-server/app/core/logging.py

**체크포인트**: 사용자 스토리 구현을 시작할 수 있는 기반이 준비된다.

---

## Phase 3: 사용자 스토리 1 - 회원가입, 로그인, 상담 시작 (우선순위: P1)

**목표**: 사용자가 계정을 만들고, 인증하고, token을 갱신하고, 로그아웃하며, 상담 시작 가능 상태에 도달한다.

**독립 테스트**: 메인 화면, 회원가입, 로그인, refresh token, 로그아웃, 내 정보 조회를 검증한다.

### 사용자 스토리 1 테스트

- [X] T020 [P] [US1] 인증 contract test를 작성한다: backend/src/test/java/com/koreatarot/auth/controller/AuthContractTest.java
- [X] T021 [P] [US1] 내 정보 조회 통합 테스트를 작성한다: backend/src/test/java/com/koreatarot/user/controller/UserProfileIntegrationTest.java
- [ ] T022 [P] [US1] 프론트엔드 회원가입/로그인 E2E test를 작성한다: front/tests/auth.spec.ts

### 사용자 스토리 1 구현

- [X] T023 [P] [US1] User entity와 repository를 만든다: backend/src/main/java/com/koreatarot/user/entity/User.java, backend/src/main/java/com/koreatarot/user/repository/UserRepository.java
- [X] T024 [US1] 비밀번호 hashing과 검증을 구현한다: backend/src/main/java/com/koreatarot/auth/service/PasswordService.java, backend/src/main/java/com/koreatarot/auth/service/PasswordValidator.java
- [X] T025 [US1] JWT와 refresh token service를 구현한다: backend/src/main/java/com/koreatarot/global/security/JwtTokenService.java, backend/src/main/java/com/koreatarot/global/security/RefreshTokenService.java
- [X] T026 [US1] 회원가입, 로그인, refresh, logout service를 구현한다: backend/src/main/java/com/koreatarot/auth/service/AuthService.java
- [X] T027 [US1] 인증 controller와 DTO를 구현한다: backend/src/main/java/com/koreatarot/auth/controller/AuthController.java
- [X] T028 [US1] 내 정보 API를 구현한다: backend/src/main/java/com/koreatarot/user/controller/UserController.java
- [X] T029 [US1] 인증 API 모듈을 구현한다: front/src/api/auth.ts
- [X] T030 [US1] 인증 상태 저장소를 구현한다: front/src/stores/authStore.ts
- [X] T031 [US1] 메인, 회원가입, 로그인 화면을 구현한다: front/app/page.tsx, front/app/signup/page.tsx, front/app/login/page.tsx

**체크포인트**: 인증 기능을 독립적으로 사용할 수 있다.

---

## Phase 4: 사용자 스토리 2 - 고민 입력 및 서버 셔플 카드 3장 선택 (우선순위: P1)

**목표**: 인증된 사용자가 유효한 고민을 제출하고, 카드 정체가 노출되지 않은 draft deck을 받은 뒤, 서로 다른 3개 위치를 선택한다.

**독립 테스트**: draft 생성, 응답 비공개성, 유효/무효 카드 선택을 검증한다.

### 사용자 스토리 2 테스트

- [ ] T032 [P] [US2] draft와 selection contract test를 작성한다: backend/src/test/java/com/koreatarot/consultation/DraftContractTest.java
- [X] T033 [P] [US2] 고민 validator test를 작성한다: backend/src/test/java/com/koreatarot/consultation/service/ConcernValidatorTest.java
- [ ] T034 [P] [US2] 프론트엔드 고민 입력 및 카드 선택 E2E test를 작성한다: front/tests/card-selection.spec.ts

### 사용자 스토리 2 구현

- [X] T035 [P] [US2] TarotCard와 CardInterpretation entity를 만든다: backend/src/main/java/com/koreatarot/tarot/entity/TarotCard.java, backend/src/main/java/com/koreatarot/tarot/entity/CardInterpretation.java
- [X] T036 [US2] 카드 목록 API를 구현한다: backend/src/main/java/com/koreatarot/tarot/controller/TarotCardController.java
- [X] T037 [US2] 고민 validator를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/ConcernValidator.java
- [X] T038 [US2] DraftDeck Redis service를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/DraftDeckService.java
- [X] T039 [US2] draft 생성 엔드포인트를 구현한다: backend/src/main/java/com/koreatarot/consultation/controller/ConsultationDraftController.java
- [X] T040 [US2] 카드 선택 검증을 구현한다: backend/src/main/java/com/koreatarot/consultation/service/CardSelectionValidator.java
- [X] T041 [US2] 상담 API module을 구현한다: front/src/api/consultations.ts
- [X] T042 [US2] 고민 입력 화면을 구현한다: front/app/consultations/new/page.tsx
- [X] T043 [US2] 카드 선택 화면을 구현한다: front/app/consultations/select/page.tsx

**체크포인트**: AI 생성 없이 고민 입력과 카드 선택 흐름이 동작한다.

---

## Phase 5: 사용자 스토리 3 - AI 타로 해석 및 진행 상태 확인 (우선순위: P1)

**목표**: 사용자가 상담을 생성하고, 진행 상황을 확인하며, 저장된 구조화 AI 결과를 받는다.

**독립 테스트**: draft로 상담을 만들고, SSE를 구독하고, 최종 결과 수신과 저장 완료를 검증한다.

### 사용자 스토리 3 테스트

- [X] T044 [P] [US3] 상담 생성과 idempotency test를 작성한다: backend/src/test/java/com/koreatarot/consultation/ConsultationCreationIntegrationTest.java
- [X] T045 [P] [US3] SSE integration test를 작성한다: backend/src/test/java/com/koreatarot/consultation/ConsultationSseIntegrationTest.java
- [ ] T046 [P] [US3] AI schema test를 작성한다: ai-server/tests/test_interpretation_schema.py
- [ ] T047 [P] [US3] 프론트엔드 결과 스트리밍 E2E test를 작성한다: front/tests/streaming-result.spec.ts

### 사용자 스토리 3 구현

- [X] T048 [P] [US3] Consultation과 ConsultationCard entity를 만든다: backend/src/main/java/com/koreatarot/consultation/entity/Consultation.java, backend/src/main/java/com/koreatarot/consultation/entity/ConsultationCard.java
- [X] T049 [US3] idempotency service를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/IdempotencyService.java
- [X] T050 [US3] 상담 생성 service를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/ConsultationService.java
- [X] T051 [US3] AI WebClient를 구현한다: backend/src/main/java/com/koreatarot/ai/client/AiInterpretationClient.java
- [X] T052 [US3] SSE event service와 controller를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/ConsultationEventService.java, backend/src/main/java/com/koreatarot/consultation/controller/ConsultationEventController.java
- [ ] T053 [P] [US3] AI Pydantic 스키마를 구현한다: ai-server/app/schemas/interpretation.py
- [ ] T054 [US3] AI 카드 해석 문서 조회 클라이언트를 구현한다: ai-server/app/services/card_docs.py
- [ ] T055 [US3] AI 카테고리 및 안전 분류기를 구현한다: ai-server/app/services/classifier.py
- [ ] T056 [US3] prompt template을 구현한다: ai-server/app/prompts/tarot_v1.py
- [ ] T057 [US3] streaming interpretation 엔드포인트를 구현한다: ai-server/app/api/interpretations.py
- [ ] T058 [US3] 결과 스트리밍 화면과 결과 상세 화면을 구현한다: front/app/consultations/[id]/stream/page.tsx, front/app/consultations/[id]/page.tsx
- [ ] T059 [US3] 구조화 결과 표시 컴포넌트를 구현한다: front/src/features/consultations/ResultView.tsx

**체크포인트**: 저장된 결과를 포함한 상담 생성 E2E 흐름이 동작한다.

---

## Phase 6: 사용자 스토리 4 - 상담 기록 조회 및 관리 (우선순위: P1)

**목표**: 사용자가 자신의 상담 기록을 다시 열고 원하지 않는 기록을 숨길 수 있다.

**독립 테스트**: 소유자 기록 목록, 상세, 타 사용자 접근 차단, soft delete를 검증한다.

### 사용자 스토리 4 테스트

- [ ] T060 [P] [US4] 기록 목록/상세/삭제 통합 테스트를 작성한다: backend/src/test/java/com/koreatarot/consultation/ConsultationHistoryIntegrationTest.java
- [ ] T061 [P] [US4] 타 사용자 접근 차단 test를 작성한다: backend/src/test/java/com/koreatarot/consultation/ConsultationAccessControlTest.java
- [ ] T062 [P] [US4] 프론트엔드 기록 E2E test를 작성한다: front/tests/history.spec.ts

### 사용자 스토리 4 구현

- [ ] T063 [US4] 기록 조회 service를 구현한다: backend/src/main/java/com/koreatarot/consultation/service/ConsultationHistoryService.java
- [ ] T064 [US4] 기록 목록, 상세, 삭제 엔드포인트를 구현한다: backend/src/main/java/com/koreatarot/consultation/controller/ConsultationHistoryController.java
- [ ] T065 [US4] cursor pagination을 구현한다: backend/src/main/java/com/koreatarot/consultation/dto/ConsultationCursor.java
- [ ] T066 [US4] 기록 API module을 구현한다: front/src/api/history.ts
- [ ] T067 [US4] 기록 목록 화면을 구현한다: front/app/history/page.tsx
- [ ] T068 [US4] 기록 상세 화면을 구현한다: front/app/history/[id]/page.tsx

**체크포인트**: 본인 기록 조회와 soft delete가 동작한다.

---

## Phase 7: 사용자 스토리 5 - 계정 생명주기와 안전 가드레일 (우선순위: P2)

**목표**: 사용자가 계정을 관리하고, 서비스가 안전/개인정보/rate limit/AI logging 제어를 적용한다.

**독립 테스트**: 탈퇴, 탈퇴 후 상담 생성 차단, 안전 문구, 민감 주제 처리, sanitize된 AI 로그를 검증한다.

### 사용자 스토리 5 테스트

- [ ] T069 [P] [US5] 회원 탈퇴 통합 테스트를 작성한다: backend/src/test/java/com/koreatarot/user/controller/UserWithdrawalIntegrationTest.java
- [ ] T070 [P] [US5] rate-limit integration test를 작성한다: backend/src/test/java/com/koreatarot/global/RateLimitIntegrationTest.java
- [ ] T071 [P] [US5] AI safety test를 작성한다: ai-server/tests/test_safety.py
- [ ] T072 [P] [US5] 프론트엔드 마이페이지 E2E test를 작성한다: front/tests/my-page.spec.ts

### 사용자 스토리 5 구현

- [ ] T073 [US5] 회원 탈퇴 service를 구현한다: backend/src/main/java/com/koreatarot/user/service/UserWithdrawalService.java
- [ ] T074 [US5] rate limiting filter와 policy를 구현한다: backend/src/main/java/com/koreatarot/global/ratelimit/RateLimitConfig.java, backend/src/main/java/com/koreatarot/global/ratelimit/RateLimitFilter.java
- [ ] T075 [US5] AI 요청 로그 엔티티와 서비스를 구현한다: backend/src/main/java/com/koreatarot/ai/service/AiRequestLogService.java
- [ ] T076 [US5] AI 민감 주제 안전 처리를 구현한다: ai-server/app/services/safety.py
- [ ] T077 [US5] 마이페이지를 구현한다: front/app/me/page.tsx
- [ ] T078 [US5] 필수 안전 및 개인정보 안내 문구 컴포넌트를 추가한다: front/src/features/consultations/SafetyNotice.tsx, front/src/features/consultations/PrivacyNotice.tsx

**체크포인트**: 계정 생명주기와 안전 가드레일이 적용된다.

---

## 최종 Phase: 품질 보강 및 공통 검증

**목적**: 전체 MVP 품질, 관찰성, 배포 가능성을 검증한다.

- [X] T079 [P] 백엔드 OpenAPI 문서 설정을 추가한다: backend/src/main/java/com/koreatarot/global/openapi/OpenApiConfig.java
- [ ] T080 [P] AI golden set 평가 script를 추가한다: ai-server/tests/golden/test_golden_set.py, ai-server/tests/golden/golden_set.json
- [ ] T081 [P] Playwright 전체 핵심 흐름 E2E test를 추가한다: front/tests/core-flow.spec.ts
- [ ] T082 k6 상담 스트리밍 부하 test를 추가한다: tests/load/consultation-stream.js
- [ ] T083 Sentry, Prometheus, Grafana, Langfuse 연동 자리표시자를 추가한다: backend/src/main/resources/application-observability.yml, ai-server/app/core/observability.py, docker-compose.yml
- [ ] T084 quickstart 검증을 실행한다: specs/001-ai-tarot-consultation/quickstart.md
- [ ] T085 로컬 실행과 MVP 범위를 README에 정리한다: README.md

---

## 의존성 및 실행 순서

### Phase 의존성

- **Phase 1 설정**: 선행 조건 없음.
- **Phase 2 기반 작업**: Phase 1 완료 후 진행하며 모든 사용자 스토리를 막는 선행 조건이다.
- **US1**: Phase 2 이후 시작하며 인증 사용자 context를 제공한다.
- **US2**: Phase 2 이후 시작할 수 있지만 인증된 draft 소유권을 위해 US1이 필요하다.
- **US3**: US2의 draft와 selection 동작이 필요하다.
- **US4**: US3의 저장된 상담이 필요하다.
- **US5**: US1 이후 시작할 수 있으며 MVP 안정화 전에 완료해야 한다.
- **최종 Phase**: 출시 대상 사용자 스토리 완료 후 진행한다.

### 병렬 기회

- T005-T006은 병렬로 진행할 수 있다.
- T011-T012, T017-T019는 backend/frontend/AI skeleton 이후 병렬로 진행할 수 있다.
- 각 사용자 스토리의 test 작업은 병렬로 작성할 수 있다.
- AI 서버 스키마와 분류기 작업은 contract 안정화 이후 backend consultation entity 작업과 병렬로 진행할 수 있다.

## 구현 전략

### MVP 우선

1. Phase 1과 Phase 2를 완료한다.
2. US1 인증을 완료한다.
3. US2 고민 입력과 draft selection을 완료한다.
4. US3 상담 생성을 완료한다.
5. US4 기록 조회를 완료한다.
6. 출시에 필요한 US5 안전/계정 제어를 추가한다.

### 검증 게이트

구현 완료 전 다음을 검증한다.

- 회원가입, 로그인, refresh, logout.
- draft 비공개성과 만료.
- 중복 selection과 idempotency 처리.
- SSE meta, token, done, error event.
- 구조화 AI 결과 저장.
- 본인 전용 목록, 상세, stream, 삭제.
- 민감 주제 안전 처리.
- 로그에 비밀값과 고민 원문 전체가 남지 않는지 여부.
