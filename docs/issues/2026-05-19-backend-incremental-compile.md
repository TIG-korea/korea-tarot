# 백엔드 Gradle Java 컴파일 classpath 문제

## 상태

- 발견일: 2026-05-19
- 상태: 해결 완료
- 관련 작업: `T035`, `T036`, `T051`

## 증상

새 도메인 패키지나 새 Java 파일을 추가한 직후 `backend`에서 `gradlew.bat test`를 실행하면 `compileJava` 또는 `compileTestJava` 단계에서 기존 패키지를 찾지 못하는 오류가 반복됐다.

대표 오류:

```text
package com.koreatarot.global.error does not exist
package com.koreatarot.auth.service does not exist
cannot find symbol: class ConsultationService
```

`gradlew.bat clean test`를 실행하면 통과했고, 이후 `gradlew.bat test`도 통과했기 때문에 처음에는 Gradle 증분 컴파일 캐시 문제처럼 보였다.

## 원인

이 Windows/JDK 21 검증 환경의 `javac`가 classpath에 있는 디렉터리 출력물(`build/classes/java/main`)을 안정적으로 읽지 못했다.

영향은 두 곳에서 나타났다.

1. `compileJava` 증분 컴파일은 새 Java 파일만 컴파일하면서 기존 클래스는 `build/classes/java/main`에서 읽는다. 이때 기존 패키지를 못 찾아 실패했다.
2. `compileTestJava`는 테스트 소스를 컴파일하면서 main 클래스들을 `build/classes/java/main`에서 읽는다. 이때도 기존 패키지를 못 찾아 실패했다.

기존 `build.gradle`에는 다음 우회가 있었다.

```groovy
tasks.named('compileTestJava') {
    source += sourceSets.main.java
    classpath = configurations.testCompileClasspath
}
```

이 설정은 테스트 컴파일 문제는 피했지만, `compileJava` 증분 컴파일 문제까지 막지는 못했다.

## 해결

Java 증분 컴파일을 끄고, 테스트 컴파일은 main 출력 디렉터리를 classpath로 읽지 않도록 main 소스를 함께 컴파일하게 유지했다.

실제 적용:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.incremental = false
}

tasks.named('compileTestJava') {
    source += sourceSets.main.java
    classpath = configurations.testCompileClasspath
}
```

## 검증 기준

다음 명령이 모두 성공해야 한다.

```bash
gradlew.bat clean test
gradlew.bat test
```

새 패키지 추가 직후에도 `clean` 없이 `gradlew.bat test`가 통과해야 한다.
