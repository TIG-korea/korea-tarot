# 백엔드 Gradle 검증 실행 환경 정리 필요

## 상태

- GitHub Issue 생성 시도: 실패
- 실패 원인: GitHub 커넥터 계정이 `TIG-korea/korea-tarot` 저장소에 `pull` 권한만 보유
- 로컬 대체 시도: `gh` CLI 없음, Git Credential Manager 비대화형 조회 타임아웃
- 관련 커밋: `3efd53c feat(global): add swagger documentation setup`

## 배경

Swagger/OpenAPI 문서화 작업 후 백엔드 컴파일과 테스트를 실행하려 했지만, 현재 저장소에 `backend/gradlew`가 없고 작업 환경에도 `gradle` 명령이 설치되어 있지 않아 `gradle test` 검증을 수행하지 못했다.

## 영향

- Java 소스 컴파일 검증을 로컬에서 즉시 수행할 수 없다.
- Swagger composed annotation 적용 여부를 실제 빌드로 확인하지 못했다.
- 이후 도메인 구현마다 검증 절차가 반복적으로 막힐 수 있다.

## 확인 필요

- 프로젝트에 Gradle Wrapper를 추가할지 결정해야 한다.
- AGENTS 규칙상 `gradle wrapper` 등 빌드 도구 변경은 사용자 확인 후 진행해야 한다.
- Wrapper를 추가하지 않는다면 개발 환경 전제 조건에 로컬 Gradle 설치를 명시해야 한다.

## 권장 해결책

1. 사용자 확인 후 `backend/gradlew`, `backend/gradlew.bat`, `backend/gradle/wrapper/`를 추가한다.
2. 이후 백엔드 검증 명령을 `backend/gradlew test`로 통일한다.
3. CI에서도 동일한 wrapper 기반 명령을 사용한다.

## GitHub Issue 전환 방법

권한이 있는 환경에서 다음 제목과 본문으로 GitHub Issue를 생성한다.

제목:

```text
백엔드 Gradle 검증 실행 환경 정리 필요
```

본문:

```markdown
Swagger/OpenAPI 문서화 작업 후 백엔드 컴파일과 테스트를 실행하려 했지만, 현재 저장소에 `backend/gradlew`가 없고 작업 환경에도 `gradle` 명령이 설치되어 있지 않아 `gradle test` 검증을 수행하지 못했습니다.

영향:
- Java 소스 컴파일 검증을 로컬에서 즉시 수행할 수 없습니다.
- Swagger composed annotation 적용 여부를 실제 빌드로 확인하지 못했습니다.
- 이후 도메인 구현마다 검증 절차가 반복적으로 막힐 수 있습니다.

확인 필요:
- 프로젝트에 Gradle Wrapper를 추가할지 결정해야 합니다.
- AGENTS 규칙상 `gradle wrapper` 등 빌드 도구 변경은 사용자 확인 후 진행해야 합니다.
- Wrapper를 추가하지 않는다면 개발 환경 전제 조건에 로컬 Gradle 설치를 명시해야 합니다.

관련 커밋:
- `3efd53c feat(global): add swagger documentation setup`
```
