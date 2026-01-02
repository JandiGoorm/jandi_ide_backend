# 보안 취약점 분석 보고서

## 개요

본 문서는 JANDI IDE Backend 코드베이스에서 발견된 보안 취약점을 기술한다. 각 취약점에 대해 위험도, 영향 범위, 발생 위치, 상세 분석, 해결 방안을 포함한다.

**최종 업데이트:** 2024년 취약점 수정 작업 완료

---

## 취약점 목록

| ID | 취약점 | 위험도 | 상태 |
|----|--------|--------|------|
| V-001 | 코드 인젝션 / RCE | 심각 | 미해결 (인프라 필요) |
| V-002 | 입력 검증 누락 | 심각 | **해결됨** |
| V-003 | 경쟁 상태 (Race Condition) | 높음 | **해결됨** |
| V-004 | JWT 내 민감 정보 저장 | 높음 | 미해결 (프론트 변경 필요) |
| V-005 | 프로세스 리소스 누수 | 높음 | **해결됨** |
| V-006 | 민감 정보 로깅 | 중간 | **해결됨** |
| V-007 | @Transactional 누락 | 중간 | 미해결 |
| V-008 | RestTemplate 매번 생성 | 중간 | **해결됨** |
| V-009 | 파일 정리 실패 무시 | 중간 | **해결됨** |
| V-010 | CORS localhost 허용 | 낮음 | 미해결 (설정 변경) |

---

## V-001: 코드 인젝션 / RCE (Remote Code Execution)

### 위험도
**심각 (Critical)**

### 발생 위치
- `compiler/service/CompilerService.java`
- `compiler/service/JavaCompiler.java`
- `compiler/service/PythonCompiler.java`
- `compiler/service/CppCompiler.java`

### 상세 분석

사용자가 제출한 코드를 서버에서 직접 컴파일 및 실행한다. 코드 내용에 대한 검증 없이 `ProcessBuilder`를 통해 시스템 명령어를 실행하므로 다음과 같은 공격이 가능하다:

```java
// 현재 구현
javaFile = new File("Main.java");
try (FileWriter writer = new FileWriter(javaFile)) {
    writer.write(code);  // 검증 없이 사용자 입력 저장
}
ProcessBuilder runPb = new ProcessBuilder("java", "Main");
```

**공격 시나리오:**
1. 파일 시스템 공격: `Runtime.getRuntime().exec("rm -rf /")`
2. 역방향 쉘: 외부 서버로 연결하여 원격 제어 획득
3. 서버 리소스 고갈: 무한 루프, fork bomb
4. 환경 변수 탈취: `System.getenv()` 호출로 DB 비밀번호 등 노출
5. 내부 네트워크 스캔: 서버가 위치한 VPC 내 다른 서비스 탐색

### 해결 방안

**단기 (권장):**
1. Docker 컨테이너 격리
   - 코드 실행 전용 컨테이너 생성
   - `--network none` 옵션으로 네트워크 차단
   - `--read-only` 옵션으로 파일 시스템 보호
   - `--memory`, `--cpus` 옵션으로 리소스 제한

2. seccomp 프로필 적용
   - 허용된 시스템 콜만 실행 가능하도록 제한

**장기:**
1. 별도 코드 실행 서비스 분리
2. Kubernetes Job으로 격리된 환경에서 실행
3. Firecracker 또는 gVisor 같은 마이크로 VM 사용

### 비고
인프라 변경이 필요하므로 별도 작업으로 진행한다.

---

## V-002: 입력 검증 누락

### 위험도
**심각 (Critical)**

### 발생 위치
- `compiler/dto/CodeSubmissionDto.java`
- 기타 DTO 클래스

### 상세 분석

코드 제출 DTO에 입력 검증 어노테이션이 없다:

```java
@Schema(description = "제출한 코드")
private String code;  // 검증 없음

@Schema(description = "프로그래밍 언어")
private String language;  // 검증 없음
```

**발생 가능한 문제:**
1. `code`가 `null`인 경우 NullPointerException 발생
2. `code`가 수 MB 이상인 경우 메모리 부족
3. `language`에 임의 값 전달 시 예외 발생
4. `userId`에 음수 전달 시 비정상 동작

### 해결 방안

```java
@NotBlank(message = "코드는 필수 항목이다")
@Size(max = 50000, message = "코드는 50KB를 초과할 수 없다")
private String code;

@NotBlank(message = "언어는 필수 항목이다")
@Pattern(regexp = "^(java|python|c\\+\\+)$", message = "지원 언어: java, python, c++")
private String language;

@NotNull(message = "사용자 ID는 필수 항목이다")
@Positive(message = "사용자 ID는 양수여야 한다")
private Long userId;
```

컨트롤러에서 `@Valid` 어노테이션 적용:

```java
@PostMapping("/compile")
public ResponseEntity<?> compile(@Valid @RequestBody CodeSubmissionDto dto) {
    // ...
}
```

---

## V-003: 경쟁 상태 (Race Condition)

### 위험도
**높음 (High)**

### 발생 위치
- `compiler/service/CompilerService.java` (테스트 모드)

### 상세 분석

테스트 모드에서 고정된 파일명을 사용한다:

```java
javaFile = new File("Main.java");  // 모든 요청이 동일 파일 사용
```

동시에 여러 사용자가 코드를 제출하면:
1. 사용자 A의 코드가 `Main.java`에 저장됨
2. 사용자 B의 코드가 `Main.java`를 덮어씀
3. 사용자 A의 실행 결과에 사용자 B의 코드가 반영됨

### 해결 방안

UUID 기반의 고유 디렉토리 사용:

```java
private Solution handleSimpleCompilationCheck(User user, CodeSubmissionDto submissionDto) {
    String uniqueId = UUID.randomUUID().toString();
    Path workDir = Paths.get("compiler_workspace", "test", uniqueId);
    Files.createDirectories(workDir);
    
    File javaFile = workDir.resolve("Main.java").toFile();
    // ...
    
    // finally 블록에서 디렉토리 정리
    FileUtils.deleteDirectory(workDir.toFile());
}
```

---

## V-004: JWT 내 민감 정보 저장

### 위험도
**높음 (High)**

### 발생 위치
- `security/JwtTokenProvider.java`

### 상세 분석

GitHub OAuth 토큰을 JWT 클레임에 저장한다:

```java
String token = Jwts.builder()
    .setSubject(githubId)
    .claim("githubToken", githubToken)  // 민감 정보
    ...
```

JWT는 암호화가 아닌 서명만 적용된다. Base64 디코딩으로 내용 확인이 가능하다. 토큰 탈취 시 GitHub 계정 접근이 가능해진다.

### 해결 방안

GitHub 토큰을 서버 측에 저장:

```java
// Redis 또는 DB에 저장
@Service
public class TokenStorageService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void storeGithubToken(String userId, String githubToken) {
        redisTemplate.opsForValue().set(
            "github_token:" + userId, 
            githubToken,
            Duration.ofDays(7)
        );
    }
    
    public String getGithubToken(String userId) {
        return redisTemplate.opsForValue().get("github_token:" + userId);
    }
}
```

JWT에는 참조 ID만 저장:

```java
String token = Jwts.builder()
    .setSubject(githubId)
    .claim("userId", user.getId())  // GitHub 토큰 제거
    .claim("role", roleName)
    ...
```

### 비고
프론트엔드 토큰 처리 로직 변경이 필요할 수 있으므로 별도 작업으로 진행한다.

---

## V-005: 프로세스 리소스 누수

### 위험도
**높음 (High)**

### 발생 위치
- `compiler/service/CompilerService.java`
- `compiler/service/JavaCompiler.java`
- `compiler/service/CppCompiler.java`
- `compiler/service/PythonCompiler.java`

### 상세 분석

예외 발생 시 프로세스가 종료되지 않는 경우가 있다:

```java
Process runProcess = runPb.start();
// 예외 발생 시 runProcess.destroy() 호출 누락
// 타임아웃 발생 시에도 프로세스가 남아있을 수 있음
```

좀비 프로세스가 누적되면 서버 리소스가 고갈된다.

### 해결 방안

try-with-resources 패턴 또는 finally 블록에서 프로세스 정리:

```java
Process runProcess = null;
try {
    runProcess = runPb.start();
    // ...
} finally {
    if (runProcess != null) {
        runProcess.destroyForcibly();
        try {
            runProcess.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## V-006: 민감 정보 로깅

### 위험도
**중간 (Medium)**

### 발생 위치
- `user/service/UserService.java`

### 상세 분석

```java
log.info("GitHub 인증 코드: {}, client_id: {}", code, githubClientId.substring(0, 5) + "...");
log.info("GitHub 액세스 토큰 길이: {}", accessToken.length());
```

프로덕션 환경에서 민감 정보가 로그 파일에 기록된다. 로그 수집 시스템(ELK, CloudWatch 등)으로 전송 시 유출 위험이 있다.

### 해결 방안

로그 레벨을 DEBUG로 변경하거나 민감 정보 제거:

```java
log.debug("GitHub 인증 처리 시작");
log.debug("GitHub 토큰 수신 완료");
```

---

## V-007: @Transactional 누락

### 위험도
**중간 (Medium)**

### 발생 위치
- 다수의 Service 클래스

### 상세 분석

트랜잭션 경계가 명확하지 않아 다음 문제가 발생할 수 있다:
1. 여러 DB 작업 중 일부만 커밋되는 부분 실패
2. 읽기 작업에서 불필요한 트랜잭션 오버헤드
3. 지연 로딩 시 LazyInitializationException 발생

### 해결 방안

```java
@Service
@Transactional(readOnly = true)  // 클래스 레벨 기본값
public class SomeService {
    
    @Transactional  // 쓰기 작업
    public void createSomething() { }
    
    // readOnly = true 상속
    public Something getSomething() { }
}
```

---

## V-008: RestTemplate 매번 생성

### 위험도
**중간 (Medium)**

### 발생 위치
- `user/service/UserService.java`

### 상세 분석

```java
RestTemplate restTemplate = new RestTemplate();  // 매 요청마다 생성
```

커넥션 풀을 사용하지 않아 성능 저하가 발생한다. 고부하 시 소켓 고갈 가능성이 있다.

### 해결 방안

Bean으로 등록하여 재사용:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();
    }
}
```

---

## V-009: 파일 정리 실패 무시

### 위험도
**중간 (Medium)**

### 발생 위치
- `compiler/service/CompilerService.java`

### 상세 분석

```java
} finally {
    if (javaFile != null) {
        javaFile.delete();  // 반환값 무시
    }
    new File("Main.class").delete();  // 반환값 무시
}
```

파일 삭제 실패가 무시되어 디스크 공간이 점차 고갈될 수 있다.

### 해결 방안

```java
} finally {
    if (javaFile != null && !javaFile.delete()) {
        log.warn("파일 삭제 실패: {}", javaFile.getAbsolutePath());
    }
    File classFile = new File("Main.class");
    if (classFile.exists() && !classFile.delete()) {
        log.warn("파일 삭제 실패: {}", classFile.getAbsolutePath());
    }
}
```

---

## V-010: CORS localhost 허용

### 위험도
**낮음 (Low)**

### 발생 위치
- `config/WebSocketConfig.java`

### 상세 분석

```java
.setAllowedOrigins(
    "https://jandiide.netlify.app",
    "http://localhost:3000",
    "http://localhost:5173"
)
```

프로덕션 환경에서 localhost origin이 허용되어 있다. 로컬 개발 환경에서만 필요한 설정이다.

### 해결 방안

프로파일 기반 설정 분리:

```java
@Value("${cors.allowed-origins}")
private String[] allowedOrigins;

registry.addEndpoint("/ws")
    .setAllowedOrigins(allowedOrigins)
    .withSockJS();
```

`application.properties`:
```properties
cors.allowed-origins=https://jandiide.netlify.app
```

`application-dev.properties`:
```properties
cors.allowed-origins=https://jandiide.netlify.app,http://localhost:3000,http://localhost:5173
```

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-01-02 | 1.0 | 초기 문서 작성 |
