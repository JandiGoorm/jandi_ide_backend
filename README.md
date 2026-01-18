# Jandi IDE Backend - 온라인 코딩 테스트 플랫폼

채용 프로세스를 위한 온라인 코딩 테스트 및 코드 실행 플랫폼의 백엔드 서버입니다.

## 주요 기능

- **코드 컴파일/실행**: 다양한 언어 지원 (Java, Python, JavaScript 등)
- **알고리즘 문제**: 알고리즘 문제 출제 및 채점
- **채용 공고**: 기업 채용 공고 관리
- **프로젝트 관리**: 코딩 테스트 프로젝트 생성/관리
- **실시간 채팅**: WebSocket 기반 실시간 커뮤니케이션
- **인증/인가**: JWT 기반 인증, GitHub OAuth

---

## 기술 스택

| 구분 | 기술 |
|:-----|:-----|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.x, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.0, MongoDB, Redis |
| **Infra** | Docker, Jenkins |
| **Test** | JUnit 5, Mockito, H2 (테스트 DB) |
| **Docs** | Swagger (SpringDoc OpenAPI) |

---

## 프로젝트 구조

```
jandi_ide_backend/
├── src/main/java/com/webproject/jandi_ide_backend/
│   ├── algorithm/          # 알고리즘 문제
│   ├── chat/               # 실시간 채팅
│   ├── company/            # 기업 정보
│   ├── compiler/           # 코드 컴파일러
│   ├── jobPosting/         # 채용 공고
│   ├── project/            # 프로젝트 관리
│   ├── tech/               # 기술 스택 정보
│   ├── user/               # 사용자 관리
│   ├── redis/              # Redis 설정
│   ├── config/             # 설정
│   ├── security/           # 보안 설정
│   └── global/             # 공통 유틸리티
├── src/test/               # 테스트 코드
├── build.gradle            # Gradle 빌드 설정
├── Dockerfile              # Docker 이미지 빌드
├── Jenkinsfile             # CI/CD 파이프라인
└── README.md
```

---

## 네이티브 환경에서 실행

### 사전 요구사항

- Java 21
- MySQL 8.0
- MongoDB 6.0
- Redis

### 실행 방법

```bash
# 1. 환경변수 설정
cp src/main/resources/application.properties.example src/main/resources/application.properties
# application.properties 파일에 DB, MongoDB, Redis 정보 입력

# 2. 빌드
./gradlew clean build -x test

# 3. 실행
./gradlew bootRun

# 또는 JAR 직접 실행
java -jar build/libs/jandi_ide_backend-0.0.1-SNAPSHOT.jar
```

**접속 URL**
- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

---

## 네이티브 환경에서 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "com.webproject.jandi_ide_backend.user.service.UserServiceTest"

# 특정 테스트 메서드 실행
./gradlew test --tests "com.webproject.jandi_ide_backend.user.service.UserServiceTest.createUser_Success"

# 테스트 + 상세 로그
./gradlew test --info

# 빌드 캐시 무시하고 재실행
./gradlew clean test
```

---

## Docker 환경에서 실행

```bash
# 1. 환경변수 설정
cp src/main/resources/application.properties.example src/main/resources/application.properties
# application.properties 수정

# 2. 이미지 빌드
docker build -t jandi-ide:local .

# 3. 컨테이너 실행
docker run -d \
  --name jandi-ide \
  -p 8081:8080 \
  jandi-ide:local

# 4. 로그 확인
docker logs -f jandi-ide

# 5. 컨테이너 중지 및 삭제
docker stop jandi-ide && docker rm jandi-ide
```

> **참고**: Tomcat은 포트 8080에서 실행되므로 `-p 8081:8080`으로 호스트 8081 포트에 매핑합니다.

---

## Docker 환경에서 테스트

```bash
# 1. 테스트용 이미지 빌드
docker build --target test -t jandi-ide:test .

# 2. 테스트 실행
docker run --rm jandi-ide:test ./gradlew test

# 또는 실행 중인 컨테이너에서 테스트
docker exec jandi-ide ./gradlew test
```

---

## GHCR에 이미지 Push

### 수동 Push

```bash
# 1. GHCR 로그인
echo $GITHUB_TOKEN | docker login ghcr.io -u kyj0503 --password-stdin

# 2. application.properties 준비 (example 복사)
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 3. 이미지 빌드
docker build --platform linux/amd64 -t ghcr.io/kyj0503/jandi-plan:latest .

# 4. Push
docker push ghcr.io/kyj0503/jandi-ide:latest
```

### 자동 Push (Jenkins)

모든 브랜치에서 Push 시 Jenkins가 자동으로:
1. 테스트 실행
2. Docker 이미지 빌드 (BuildKit 캐시 활용)
3. GHCR에 Push (`latest` + 빌드 번호 태그)
4. home-server 배포 트리거

---

## 커밋 컨벤션

### 기본 포맷

```
태그(스코프): 제목 (50자 내외)

- 본문 (선택 사항)
```

### 스코프 (Scope)

| 스코프 | 설명 |
|:-------|:-----|
| `be` | Backend 관련 코드 |
| `infra` | 배포, Docker, CI/CD 등 |

### 태그 (Type)

| 태그 | 설명 | 예시 |
|:-----|:-----|:-----|
| `feat` | 새로운 기능 추가 | API 개발 |
| `fix` | 버그 수정 | 로직 오류 수정 |
| `docs` | 문서 수정 | README, Swagger |
| `style` | 코드 포맷팅 | 들여쓰기 정렬 |
| `refactor` | 코드 리팩토링 | 구조 개선 |
| `test` | 테스트 코드 | 테스트 추가/수정 |
| `chore` | 기타 잡무 | 빌드 설정 |

### 예시

```
feat(be): 코드 컴파일러 Python 지원 추가
fix(be): 채팅 WebSocket 연결 오류 수정
docs(be): Swagger API 문서 업데이트
chore(infra): Dockerfile BuildKit 캐시 적용
```

---

## 운영 환경

- Jenkins 파이프라인을 통해 Docker 이미지 빌드 후 GHCR에 Push
- 운영 환경 배포는 **home-server** 리포지토리에서 중앙 관리
- 환경변수 및 시크릿: `home-server/config/jandi-ide/`
