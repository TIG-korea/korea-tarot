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

## 재발 확인

- 2026-05-19: `global` 도메인 기반 작업 중 `gradle -v` 확인 실패.
- 2026-05-19: `backend/gradlew` 파일 없음 확인.
- 영향: `T011`~`T016` 구현 후에도 로컬 백엔드 컴파일/테스트 검증을 수행하지 못함.
- 2026-05-19: `auth/user` 도메인 구현 중 동일 원인으로 컴파일/테스트 검증을 수행하지 못함.
- 영향: `T020`, `T021` 테스트 task는 작성/완료 처리하지 않고 보류함.

## 해결 시도 기록

- 2026-05-19: 사용자 승인에 따라 Gradle Wrapper 추가를 시도함.
- 우선 로컬에 사용 가능한 `gradle` 명령 또는 기존 wrapper 자원이 있는지 확인하고, 없으면 네트워크 다운로드 기반으로 wrapper 추가를 시도할 예정.
- 2026-05-19: Gradle 8.10.2 배포본을 임시 다운로드해 `backend` Gradle Wrapper 생성을 완료함.
- 2026-05-19: `backend/gradlew.bat test` 실행이 시작되어 기존 검증 환경 문제는 해결 방향으로 전환됨.
- 2026-05-19: 첫 컴파일 오류 확인. `ApiResponse` record의 static `success()`가 record accessor와 충돌해 `empty()`로 변경함.
- 2026-05-19: 수정 후 `backend/gradlew.bat test` 성공. `compileJava`, `processResources`, `classes` 통과.
- 2026-05-19: 현재 테스트 소스가 없어 `test NO-SOURCE`로 종료됨. 검증 실행 환경 문제는 해결됨.

## 확인 필요

- `T020`, `T021` 테스트 task는 별도 구현이 필요하다.
- 이후 백엔드 검증은 `backend/gradlew.bat test` 또는 `./gradlew test`로 수행한다.

## 해결 결과

1. 사용자 확인 후 `backend/gradlew`, `backend/gradlew.bat`, `backend/gradle/wrapper/`를 추가했다.
2. 백엔드 검증 명령을 `backend/gradlew.bat test`로 실행해 성공을 확인했다.
3. CI에서도 동일한 wrapper 기반 명령을 사용할 수 있다.

## GitHub Issue 전환 방법

권한이 있는 환경에서 다음 제목과 본문으로 GitHub Issue를 생성한다.

제목:

```text
백엔드 Gradle Wrapper 추가 및 검증 환경 복구 완료
```

본문:

```markdown
백엔드에 Gradle Wrapper가 없어 `gradle test` 검증을 수행하지 못하던 문제가 있었습니다.

처리 내용:
- Gradle 8.10.2 기반 wrapper를 `backend/`에 추가했습니다.
- `backend/gradlew`, `backend/gradlew.bat`, `backend/gradle/wrapper/`를 커밋 대상으로 포함했습니다.
- `backend/gradlew.bat test`를 실행해 `compileJava`, `processResources`, `classes` 통과를 확인했습니다.
- `ApiResponse` record의 static `success()` 컴파일 충돌을 발견해 `empty()`로 수정했습니다.

남은 작업:
- 현재 테스트 소스가 없어 Gradle 결과는 `test NO-SOURCE`입니다.
- `T020`, `T021` 테스트 task는 별도 구현이 필요합니다.

관련 커밋:
- `f2f494c chore(backend): add gradle wrapper`
```
