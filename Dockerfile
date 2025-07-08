# Build stage
FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src/ src/
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM openjdk:17-jdk-slim

# 1. curl을 설치 항목에 추가합니다.
RUN apt-get update && apt-get install -y \
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

# 2. 외부 application.properties를 사용하도록 ENTRYPOINT를 수정합니다.
ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "/app/app.jar"]