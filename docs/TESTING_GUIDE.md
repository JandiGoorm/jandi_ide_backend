# 테스트 코드 작성 가이드

## 개요

본 문서는 JANDI IDE Backend 프로젝트의 테스트 코드 작성 지침을 기술한다. 단위 테스트(Unit Test)를 중심으로 하며, 동등 분할(Equivalence Partitioning) 기법과 블랙박스 테스트 방식을 적용한다.

---

## 테스트 전략

### 테스트 유형

| 유형 | 비율 | 설명 |
|------|------|------|
| 단위 테스트 | 80% | 개별 클래스/메서드 수준의 테스트 |
| 통합 테스트 | 15% | 여러 컴포넌트 간 상호작용 테스트 |
| E2E 테스트 | 5% | 전체 시스템 흐름 테스트 |

### 테스트 방법론

**블랙박스 테스트**를 기본으로 한다:
- 내부 구현이 아닌 입력/출력 기준으로 테스트 설계
- 공개 API(public 메서드)만 테스트
- 명세 기반 테스트 케이스 도출

**동등 분할 테스트**를 적용한다:
- 입력 도메인을 동등 클래스로 분할
- 각 클래스에서 대표값 선택
- 경계값 분석(Boundary Value Analysis) 병행

---

## 디렉토리 구조

```
src/test/java/com/webproject/jandi_ide_backend/
├── algorithm/
│   ├── problem/
│   │   └── service/
│   │       └── ProblemServiceTest.java
│   ├── solution/
│   └── testCase/
├── compiler/
│   ├── service/
│   │   ├── CompilerServiceTest.java
│   │   ├── JavaCompilerTest.java
│   │   ├── PythonCompilerTest.java
│   │   └── CppCompilerTest.java
│   └── dto/
│       └── CodeSubmissionDtoTest.java
├── user/
│   └── service/
│       └── UserServiceTest.java
├── chat/
├── company/
└── common/
    └── TestFixtures.java
```

---

## 네이밍 규칙

### 테스트 클래스

```
{대상클래스명}Test.java
```

예시:
- `CompilerServiceTest.java`
- `UserServiceTest.java`

### 테스트 메서드

```
{메서드명}_{시나리오}_{기대결과}
```

예시:
```java
@Test
void compile_validJavaCode_returnsCorrectStatus() { }

@Test
void compile_nullCode_throwsValidationException() { }

@Test
void compile_oversizedCode_throwsValidationException() { }
```

한글 사용 시:
```java
@Test
@DisplayName("유효한 Java 코드 컴파일 시 CORRECT 상태 반환")
void compile_validJavaCode_returnsCorrectStatus() { }
```

---

## 동등 분할 테스트 설계

### 설계 절차

1. 입력 변수 식별
2. 각 변수의 유효/무효 동등 클래스 정의
3. 경계값 식별
4. 테스트 케이스 도출

### 예시: CodeSubmissionDto.code 필드

**동등 클래스:**

| 클래스 | 조건 | 유효성 |
|--------|------|--------|
| EC1 | null | 무효 |
| EC2 | 빈 문자열 ("") | 무효 |
| EC3 | 공백만 있는 문자열 | 무효 |
| EC4 | 1 ~ 50,000 바이트 | 유효 |
| EC5 | 50,001 바이트 이상 | 무효 |

**경계값:**

| 경계 | 값 | 기대 결과 |
|------|-----|----------|
| 최소 유효 | 1 바이트 | 유효 |
| 최대 유효 | 50,000 바이트 | 유효 |
| 최소 무효 | 50,001 바이트 | 무효 |

**테스트 코드:**

```java
class CodeSubmissionDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // EC1: null
    @Test
    void code_null_validationFails() {
        CodeSubmissionDto dto = CodeSubmissionDto.builder()
            .code(null)
            .language("java")
            .userId(1L)
            .build();

        Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // EC2: 빈 문자열
    @Test
    void code_emptyString_validationFails() {
        CodeSubmissionDto dto = CodeSubmissionDto.builder()
            .code("")
            .language("java")
            .userId(1L)
            .build();

        Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    // EC4: 유효 범위 (경계값 - 최소)
    @Test
    void code_singleCharacter_validationPasses() {
        CodeSubmissionDto dto = CodeSubmissionDto.builder()
            .code("a")
            .language("java")
            .userId(1L)
            .build();

        Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // EC4: 유효 범위 (경계값 - 최대)
    @Test
    void code_maxSize_validationPasses() {
        String maxCode = "a".repeat(50000);
        CodeSubmissionDto dto = CodeSubmissionDto.builder()
            .code(maxCode)
            .language("java")
            .userId(1L)
            .build();

        Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    // EC5: 초과
    @Test
    void code_exceedsMaxSize_validationFails() {
        String oversizedCode = "a".repeat(50001);
        CodeSubmissionDto dto = CodeSubmissionDto.builder()
            .code(oversizedCode)
            .language("java")
            .userId(1L)
            .build();

        Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
}
```

### 예시: CodeSubmissionDto.language 필드

**동등 클래스:**

| 클래스 | 조건 | 유효성 |
|--------|------|--------|
| EC1 | null | 무효 |
| EC2 | "java" | 유효 |
| EC3 | "python" | 유효 |
| EC4 | "c++" | 유효 |
| EC5 | 기타 문자열 ("javascript", "go" 등) | 무효 |
| EC6 | 대소문자 변형 ("JAVA", "Java") | 정책에 따름 |

---

## 단위 테스트 작성 지침

### 기본 구조 (AAA 패턴)

```java
@Test
void methodName_scenario_expectedResult() {
    // Arrange (준비)
    SomeDto input = createValidInput();
    
    // Act (실행)
    Result result = service.someMethod(input);
    
    // Assert (검증)
    assertEquals(expectedValue, result.getValue());
}
```

### Mock 사용

외부 의존성은 Mock으로 대체한다:

```java
@ExtendWith(MockitoExtension.class)
class CompilerServiceTest {

    @Mock
    private ProblemService problemService;

    @Mock
    private TestCaseService testCaseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompilerService compilerService;

    @Test
    void submitCode_validInput_returnsSolution() {
        // Arrange
        User mockUser = createMockUser();
        Problem mockProblem = createMockProblem();
        List<TestCase> mockTestCases = createMockTestCases();

        when(userService.getUserById(1L)).thenReturn(mockUser);
        when(problemService.getProblemById(1)).thenReturn(mockProblem);
        when(testCaseService.getTestCasesByProblemId(1)).thenReturn(mockTestCases);

        CodeSubmissionDto dto = createValidSubmissionDto();

        // Act
        Solution result = compilerService.submitCode(dto);

        // Assert
        assertNotNull(result);
        verify(userService).getUserById(1L);
    }
}
```

### 예외 테스트

```java
@Test
void submitCode_userNotFound_throwsCustomException() {
    // Arrange
    when(userService.getUserById(anyLong()))
        .thenThrow(new CustomException(CustomErrorCodes.USER_NOT_FOUND));

    CodeSubmissionDto dto = createValidSubmissionDto();

    // Act & Assert
    CustomException exception = assertThrows(
        CustomException.class,
        () -> compilerService.submitCode(dto)
    );

    assertEquals(CustomErrorCodes.USER_NOT_FOUND, exception.getCustomErrorCode());
}
```

---

## 테스트 픽스처

공통으로 사용하는 테스트 데이터는 별도 클래스로 관리한다:

```java
public class TestFixtures {

    public static User createMockUser() {
        User user = new User();
        user.setId(1);
        user.setGithubId("testuser");
        user.setNickname("Test User");
        user.setEmail("test@example.com");
        user.setRole(User.UserRole.USER);
        return user;
    }

    public static Problem createMockProblem() {
        Problem problem = new Problem();
        problem.setId(1);
        problem.setTitle("Test Problem");
        problem.setTimeLimit(5);
        problem.setMemory(256);
        return problem;
    }

    public static CodeSubmissionDto createValidSubmissionDto() {
        return CodeSubmissionDto.builder()
            .userId(1L)
            .problemId(1L)
            .code("public class Main { public static void main(String[] args) { } }")
            .language("java")
            .solvingTime(60)
            .build();
    }

    public static String createValidJavaCode() {
        return """
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
            """;
    }

    public static String createValidPythonCode() {
        return "print('Hello')";
    }

    public static String createValidCppCode() {
        return """
            #include <iostream>
            int main() {
                std::cout << "Hello" << std::endl;
                return 0;
            }
            """;
    }
}
```

---

## 서비스별 테스트 케이스

### CompilerService

| 메서드 | 시나리오 | 기대 결과 |
|--------|----------|----------|
| submitCode | 유효한 Java 코드 | Solution 반환, isCorrect=true |
| submitCode | 컴파일 오류 코드 | CompilerException 발생 |
| submitCode | 런타임 오류 코드 | CompilerException 발생 |
| submitCode | 시간 초과 코드 | TIMEOUT 상태 |
| submitCode | problemId=0 (테스트 모드) | 테스트케이스 없이 실행 |
| compileOnly | 유효한 코드 | CompileResultDto 반환 |
| compileOnly | null 코드 | ValidationException 발생 |

### UserService

| 메서드 | 시나리오 | 기대 결과 |
|--------|----------|----------|
| login | 유효한 GitHub 코드 | AuthResponseDTO 반환 |
| login | 무효한 GitHub 코드 | CustomException 발생 |
| login | 신규 사용자 | User 생성 후 토큰 반환 |
| login | 기존 사용자 | 토큰만 반환 |
| getUserById | 존재하는 ID | User 반환 |
| getUserById | 존재하지 않는 ID | CustomException 발생 |

### ChatRoomService

| 메서드 | 시나리오 | 기대 결과 |
|--------|----------|----------|
| createRoom | 유효한 입력 | ChatRoom 반환 |
| createRoom | 빈 이름 | IllegalArgumentException 발생 |
| findAllRooms | 방 존재 | List<ChatRoom> 반환 |
| findAllRooms | 방 없음 | 빈 리스트 반환 |
| findRoomById | 존재하는 ID | ChatRoom 반환 |
| findRoomById | 존재하지 않는 ID | null 반환 |
| deleteRoom | 존재하는 방 | true 반환 |
| deleteRoom | 존재하지 않는 방 | false 반환 |

---

## 테스트 실행

### 전체 테스트 실행

```bash
./gradlew test
```

### 특정 클래스 테스트

```bash
./gradlew test --tests "CompilerServiceTest"
```

### 특정 메서드 테스트

```bash
./gradlew test --tests "CompilerServiceTest.submitCode_validJavaCode_returnsSolution"
```

### 테스트 리포트 확인

테스트 실행 후 `build/reports/tests/test/index.html` 파일에서 결과 확인 가능.

---

## 커버리지

### 목표 커버리지

| 계층 | 라인 커버리지 | 브랜치 커버리지 |
|------|---------------|-----------------|
| Service | 80% 이상 | 70% 이상 |
| Controller | 70% 이상 | 60% 이상 |
| DTO | 90% 이상 | - |

### 커버리지 리포트 생성

```bash
./gradlew test jacocoTestReport
```

리포트 위치: `build/reports/jacoco/test/html/index.html`

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-01-02 | 1.0 | 초기 문서 작성 |
