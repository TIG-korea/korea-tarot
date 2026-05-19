# 백엔드 Gradle 증분 컴파일 재현

## 상태

- 발견일: 2026-05-19
- 상태: 우회 가능
- 관련 작업: `T035`, `T036`

## 문제

`tarot` 도메인 구현 후 `backend` 디렉터리에서 `gradlew.bat test`를 바로 실행했을 때 `compileJava` 단계가 실패했다.

대표 오류:

```text
cannot find symbol: class SecurityExceptionHandler
cannot find symbol: class JwtAuthenticationFilter
package com.koreatarot.global.api does not exist
cannot find symbol: variable OpenApiExamples
cannot find symbol: class AuthProperties
```

## 확인 결과

- 새로 추가한 `tarot` 코드 자체의 컴파일 오류는 아니었다.
- `gradlew.bat clean test`로 전체 재컴파일하면 성공했다.
- 이후 다시 `gradlew.bat test`를 실행해도 성공했다.
- 패키지 이동 또는 새 도메인 추가 직후 Gradle 증분 컴파일이 일부 기존 소스셋 클래스를 classpath에 올리지 못하는 것으로 보인다.

## 현재 우회 방법

패키지 이동 또는 새 도메인 추가 직후 비슷한 오류가 발생하면 `backend` 디렉터리에서 다음 순서로 검증한다.

```bash
gradlew.bat clean test
gradlew.bat test
```
