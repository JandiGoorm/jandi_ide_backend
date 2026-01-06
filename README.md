### 개발 주의 사항

#### 로컬 개발
1. `src/main/resources/application.properties.example` 파일을 복사하여 `application.properties`를 생성합니다.
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
2. 생성한 `application.properties` 파일에 필요한 환경 변수 값을 설정합니다.
3. `application.properties`는 `.gitignore`에 등록되어 있어 Git에 커밋되지 않습니다.

```bash
# 로컬에서 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew clean build -x test
java -jar build/libs/jandi_ide_backend-0.0.1-SNAPSHOT.jar
```

#### Docker로 실행
```bash
# 1. application.properties 준비
# src/main/resources/application.properties 파일에 실제 값을 설정하세요
cp src/main/resources/application.properties.example src/main/resources/application.properties
# application.properties 파일을 편집하여 실제 DB, MongoDB, Redis 정보 입력

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

> **참고**: 
> - Docker 빌드 시 `src/main/resources/application.properties` 파일이 이미지에 포함됩니다.
> - 로컬 테스트 시 `application.properties`에 실제 DB/MongoDB/Redis 연결 정보를 설정해야 합니다.
> - Tomcat은 포트 8080에서 실행되므로 `-p 8081:8080`으로 호스트 8081 포트에 매핑합니다.

#### 운영 환경
- Jenkins 파이프라인을 통해 Docker 이미지 빌드 후 GHCR에 Push되며, 운영 환경 배포는 **home-server** 리포지토리에서 중앙 관리합니다.
- 환경변수 및 시크릿은 `home-server/config/jandi-ide/` 디렉토리에서 관리됩니다.

### 코드 테스트 방법

```
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.webproject.jandi_ide_backend.user.service.UserServiceTest"

# 특정 테스트 메서드만 실행
./gradlew test --tests "com.webproject.jandi_ide_backend.user.service.UserServiceTest.{특정 메서드}"

# 테스트 + 상세 로그 출력
./gradlew test --info

# 실패한 테스트만 재실행
./gradlew test --rerun-tasks

# 빌드 캐시 무시하고 전체 재실행
./gradlew clean test
```

### 깃 커밋 메시지 컨벤션

1. 기본 포맷 (Format)

```
태그(스코프): 제목 (50자 내외)

- 본문 (선택 사항, 자세한 설명이 필요할 때만 작성)
```

2. 스코프 (Scope) - 위치 구분

```
be | Backend 관련 코드
infra | 배포, Docker, CI/CD 등
```

3. 태그 (Type) - 작업 성격

```
feat | 새로운 기능 추가 | API 개발, 버튼 추가
fix | 버그 수정 | 로직 오류 수정, 오타 수정
docs | 문서 수정 | README, Swagger, 주석 수정
style | 코드 포맷팅 (로직 변경 X) | 세미콜론 누락, 줄바꿈, 들여쓰기 정렬
refactor | 코드 리팩토링 | 기능 변경 없이 코드 구조 개선
test | 테스트 코드 | 테스트 코드 추가/수정 (프로덕션 코드 변경 X)
chore | 기타 잡무 | 빌드 설정, 패키지 매니저 설정, 라이브러리 추가
```
