# 백엔드 테스트 컴파일 클래스패스 이슈

## 상태

- 발견일: 2026-05-19
- 상태: 해결 완료
- 관련 작업: `T020`, `T021`

## 문제

`AuthContractTest`, `UserProfileIntegrationTest`를 추가한 뒤 `backend/gradlew.bat clean test` 실행 시 `compileTestJava` 단계에서 메인 클래스들을 찾지 못했다.

대표 오류:

```text
package com.koreatarot.global.error does not exist
cannot find symbol: class AuthService
cannot find symbol: class User
```

## 확인 내용

- `compileJava`는 정상 통과했다.
- `build/classes/java/main` 아래에 메인 클래스 파일이 생성되어 있었다.
- Gradle의 `testCompileClasspath`에도 `build/classes/java/main`이 포함되어 있었다.
- 하지만 현재 Windows/Sandbox 환경의 `javac`가 디렉터리 클래스패스의 `.class` 파일을 컴파일 중 참조하지 못했다.
- JAR 클래스패스도 `AccessDeniedException`이 발생해 테스트 컴파일에 사용할 수 없었다.

## 처리

테스트 컴파일 단계에서 메인 클래스 디렉터리나 JAR을 참조하지 않고, 메인 Java 소스를 테스트 컴파일 소스에 함께 포함하도록 `compileTestJava`를 조정했다.

추가로 테스트 컴파일에서도 Lombok annotation processor가 필요해 `testCompileOnly`, `testAnnotationProcessor`에 Lombok을 명시했다.

## 결과

다음 명령이 성공했다.

```bash
backend/gradlew.bat clean test
```

## 추후 확인

CI 또는 일반 로컬 환경에서 동일 문제가 발생하지 않는다면, 이 workaround를 제거하고 Gradle 기본 테스트 클래스패스 동작으로 되돌릴 수 있다.
