# syntax=docker/dockerfile:1
# 1. 빌드 스테이지: 표준 JDK 환경에서 프로젝트의 Gradle Wrapper를 사용해 빌드
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Ubuntu 미러를 Kakao로 변경 (apt 속도 향상)
RUN sed -i 's@archive.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list && \
    sed -i 's@security.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list

# Gradle 관련 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 실행 권한 부여
RUN chmod +x ./gradlew

# 소스 코드 복사
COPY src src

# 최종 JAR 파일 빌드 (Gradle 캐시 마운트로 재다운로드 방지)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build -x test --no-daemon

# -----------------------------------------------------

# 2. 실행 스테이지: JRE + 컴파일 환경 구성
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Ubuntu 미러를 Kakao로 변경 (다운로드 속도 향상)
RUN sed -i 's@archive.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list && \
    sed -i 's@security.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list

# 패키지 설치 (컴파일 환경 + curl for healthcheck)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gcc \
    g++ \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# compiler_workspace 디렉토리 생성
RUN mkdir -p /app/compiler_workspace

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 상태 모니터링
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 컨테이너 실행 시, 외부(/app/config/application.properties)에 있는 설정 파일을 사용하도록 지정
# 이 경로는 docker-compose.yml에 설정한 volumes 경로와 반드시 일치해야 합니다.
ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "app.jar"]