# 프로젝트 작업 규칙 보강

## 프론트엔드 UI 작업 기준

- 프론트엔드 UI는 사용자가 이미 구현한 화면과 컴포넌트를 우선 사용한다.
- 기능 구현 중 새 UI를 임의로 생성하지 않는다.
- 기존 `front/` 구조를 먼저 읽고 API 연동, 상태 관리, 라우팅, 검증, SSE 수신 같은 기능 연결만 수행한다.
- 레이아웃, 디자인, 텍스트, 스타일은 기존 구현을 최대한 유지한다.
- 필요한 최소 UI 수정이 있으면 수정 전에 어떤 파일을 수정하는지 먼저 보고한다.
- API 호출은 `front/src/api/` 내부 모듈을 통해 연결한다.
- 폼 검증은 기존 규칙대로 `front/src/utils/validators.ts` 또는 기존 검증 구조에 맞춘다.

## 백엔드 패키지 구조 기준

도메인 패키지는 역할별 하위 패키지로 나눈다. 도메인 루트에 모든 클래스를 평면 배치하지 않는다.

기본 구조:

```text
com.koreatarot.{domain}/
  controller/
  dto/
  service/
  entity/
  repository/
  enums/
  config/
  security/
```

적용 기준:

- `controller`: HTTP endpoint와 Swagger docs annotation 적용 클래스.
- `dto`: Request/Response DTO. 기존 규칙대로 관련 Request/Response는 하나의 DTO 클래스 안에 inner record로 묶는다.
- `service`: 비즈니스 흐름, 트랜잭션, 외부 저장소 연동 로직.
- `entity`: JPA Entity.
- `repository`: Spring Data Repository.
- `enums`: 상태값, 코드값 enum.
- `config`: 도메인 설정 properties/config.
- `security`: 해당 도메인 내부에서만 쓰는 보안 정책. JWT 인증/인가처럼 여러 도메인에서 공유되는 인프라는 `global/security`에 둔다.

전역 보안 구조:

```text
global/security/
  AuthenticatedUser.java
  AuthProperties.java
  JwtAuthenticationFilter.java
  JwtTokenService.java
  RefreshTokenService.java
  SecurityConfig.java
  SecurityExceptionHandler.java
```

현재 정리된 예시:

```text
auth/
  controller/AuthController.java
  dto/AuthDto.java
  service/AuthService.java
  service/PasswordService.java
  service/PasswordValidator.java

user/
  controller/UserController.java
  dto/UserDto.java
  entity/User.java
  repository/UserRepository.java
  enums/UserStatus.java
```

다음 도메인(`tarot`, `consultation`, `ai`)도 위 구조를 따른다. 단, 해당 역할의 클래스가 아직 없으면 빈 패키지를 만들지 않는다.

## 구현 진행 기준

- 모든 기능 구현은 `specs/` 문서를 기준으로 진행한다.
- spec에 없는 기능은 임의로 추가하지 않는다.
- 현재 도메인에 필요한 최소 범위만 구현하고 오버코딩하지 않는다.
- 구현 중 이슈, 검증 불가, 위험 요소가 생기면 즉시 사용자에게 알린다.
- GitHub Issue 생성 권한이 없으므로 unresolved 항목은 `docs/issues/`에 문서화한다.
