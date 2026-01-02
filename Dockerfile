# syntax=docker/dockerfile:1.4

# Build stage - 의존성과 소스를 분리하여 캐시 효율 극대화
FROM gradle:8.4-jdk17 AS build

WORKDIR /app

# 1단계: Gradle 래퍼와 설정 파일만 복사 (변경 빈도 낮음)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# 2단계: 의존성만 먼저 다운로드 (소스 변경 시 캐시 재사용)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# 3단계: 소스 복사 및 빌드
COPY src/ src/
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre

# 패키지 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gcc \
    g++ \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
RUN mkdir -p /app/compiler_workspace
COPY --from=build /app/build/libs/*.jar app.jar
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "/app/app.jar"]