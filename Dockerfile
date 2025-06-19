# Multi-stage build for Java 17 Spring Boot application
FROM gradle:8.4-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy gradle files for dependency resolution
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Grant execution permission to gradlew
RUN chmod +x gradlew

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM openjdk:17-jdk-slim

# Install necessary packages for compiler functionality
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    python3 \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create directories for compiler workspace
RUN mkdir -p /app/compiler_workspace

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create a non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring

# Expose port (Spring Boot default is 8080)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 