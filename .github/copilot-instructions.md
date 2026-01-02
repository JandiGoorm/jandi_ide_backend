# JANDI IDE Backend - Copilot Instructions

## 프로젝트 개요
JANDI IDE는 알고리즘 코딩 테스트 연습을 위한 웹 기반 IDE 플랫폼의 백엔드입니다. Java/Python/C++ 코드 컴파일 및 실행, 실시간 채팅, 채용 공고 관리 기능을 제공합니다.

## 아키텍처
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Spring Boot    │────▶│    MySQL/JPA    │     │    MongoDB      │
│  REST API       │     │  (사용자/기업)   │     │  (채팅 메시지)   │
└────────┬────────┘     └─────────────────┘     └─────────────────┘
         │
         ├──────────────┬──────────────────────────────────────────┐
         ▼              ▼                                          ▼
┌─────────────────┐  ┌─────────────────┐                 ┌─────────────────┐
│  Redis          │  │  WebSocket      │                 │  Compiler       │
│  (채팅방 관리)   │  │  (STOMP 채팅)   │                 │  (코드 실행)    │
└─────────────────┘  └─────────────────┘                 └─────────────────┘
```

## 핵심 도메인 모듈
- **compiler/** - 코드 컴파일/실행 (Java, Python, C++). `CompilerService`에서 언어별 컴파일러 분리
- **algorithm/** - 문제(Problem), 문제집(ProblemSet), 테스트케이스(TestCase), 솔루션(Solution) 관리
- **chat/** - WebSocket STOMP 기반 실시간 채팅. 채팅방은 Redis에, 메시지는 MongoDB에 저장
- **user/** - GitHub OAuth 로그인, JWT 인증
- **company/**, **jobPosting/** - 기업 및 채용 공고 관리

## 코드 컨벤션

### 계층 구조 (도메인별 동일 패턴)
```
{domain}/
├── controller/  - REST API 엔드포인트 (@RestController)
├── dto/         - 요청/응답 DTO
├── entity/      - JPA 엔티티
├── repository/  - Spring Data JPA Repository
└── service/     - 비즈니스 로직 (@Service)
```

### 예외 처리
- 커스텀 예외: `CustomException(CustomErrorCodes.XXX)` 사용
- 에러 코드 정의: [global/error/CustomErrorCodes.java](src/main/java/com/webproject/jandi_ide_backend/global/error/CustomErrorCodes.java)
- 전역 핸들러: `GlobalExceptionHandler`가 모든 예외를 처리하여 표준화된 응답 반환

### 보안 및 인증
- JWT 인증: `JwtTokenProvider`로 토큰 생성/검증, `JwtAuthenticationFilter`로 필터링
- 역할 기반 접근 제어: `USER`, `STAFF`, `ADMIN` 역할 ([SecurityConfig.java](src/main/java/com/webproject/jandi_ide_backend/config/SecurityConfig.java) 참조)
- WebSocket 인증: STOMP 메시지에 `Authorization` 헤더로 JWT 전달

### API 문서
- Swagger/OpenAPI 3.0 사용 - 모든 컨트롤러에 `@Tag`, `@Operation`, `@ApiResponse` 어노테이션 필수
- 접근: `/swagger-ui/**`, `/v3/api-docs/**`

## 빌드 및 실행

```bash
# 빌드 (테스트 제외)
./gradlew build -x test

# 로컬 실행 (환경변수 필요)
./gradlew bootRun

# Docker 빌드 및 실행
docker build -t jandi-ide-backend .
docker run -p 8080:8080 -v /path/to/config:/app/config jandi-ide-backend
```

### 필수 환경변수
```properties
DB_URL, DB_USERNAME, DB_PASSWORD    # MySQL
MONGO_DB                            # MongoDB URI
REDIS_PW                            # Redis 비밀번호
GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET  # OAuth
JWT_SECRET                          # JWT 서명 키 (32자 이상)
```

## 주요 통합 지점

### 컴파일러 서비스
- 코드 실행 시 `compiler_workspace/` 디렉토리에 임시 파일 생성
- 언어별 컴파일러: `JavaCompiler`, `PythonCompiler`, `CppCompiler` 클래스
- 테스트 모드: `problemId=0`으로 요청 시 테스트케이스 없이 컴파일/실행만 확인

### 채팅 시스템
- WebSocket 연결: `/ws` 엔드포인트 (SockJS 폴백 지원)
- 메시지 발행: `/app/chat/message`, 구독: `/topic/chat/room/{roomId}`
- 상세 API: [docs/README-websocket.md](docs/README-websocket.md), [docs/README-chatroom.md](docs/README-chatroom.md)

## 보안 관련
- 보안 취약점 분석: [docs/SECURITY_VULNERABILITIES.md](docs/SECURITY_VULNERABILITIES.md) 참조
- DTO 입력 검증: `@Valid`, `@NotBlank`, `@Size`, `@Pattern` 어노테이션 필수
- 민감 정보 로깅 금지: GitHub 토큰, 비밀번호 등은 `log.debug` 레벨만 허용
- RestTemplate은 Bean 주입으로 재사용 (매번 생성 금지)

## 테스트 작성
- 테스트 가이드: [docs/TESTING_GUIDE.md](docs/TESTING_GUIDE.md) 참조
- 블랙박스 테스트 + 동등 분할 기법 적용
- 메서드명 규칙: `{메서드명}_{시나리오}_{기대결과}`

## 주의사항
- JPA `ddl-auto=validate` 설정 - 스키마 변경 시 마이그레이션 필요
- Redis 연결 실패 시 채팅방 기능 불가
- 컴파일러는 Docker 컨테이너 내에서 gcc, g++, python3 필요
