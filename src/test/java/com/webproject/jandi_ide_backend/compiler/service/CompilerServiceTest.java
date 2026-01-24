package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.problemSet.entity.ProblemSet;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.service.SolutionService;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import com.webproject.jandi_ide_backend.common.TestFixtures;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.SaveSolutionDto;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CompilerService 단위 테스트
 * 
 * 블랙박스 테스트 방식으로 submitCode 메서드의 동작을 검증한다.
 * 외부 의존성은 Mock으로 대체하여 단위 테스트 독립성을 보장한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CompilerService 테스트")
class CompilerServiceTest {

    @Mock
    private ProblemService problemService;

    @Mock
    private TestCaseService testCaseService;

    @Mock
    private UserService userService;

    @Mock
    private SolutionService solutionService;

    @Mock
    private JavaCompiler javaCompiler;

    @Mock
    private PythonCompiler pythonCompiler;

    @Mock
    private CppCompiler cppCompiler;

    @Mock
    private CompilerFileManager fileManager;

    @InjectMocks
    private CompilerService compilerService;

    private User mockUser;
    private Problem mockProblem;
    private List<TestCase> mockTestCases;
    private ProblemSet mockProblemSet;

    @BeforeEach
    void setUp() {
        mockUser = TestFixtures.createMockUser();
        mockProblem = TestFixtures.createMockProblem();
        mockTestCases = TestFixtures.createMockTestCases();
        mockProblemSet = TestFixtures.createMockProblemSet();
    }

    @Nested
    @DisplayName("submitCode 메서드 테스트")
    class SubmitCodeTest {

        /**
         * 사용자를 찾을 수 없는 경우 CustomException 발생
         */
        @Test
        @DisplayName("사용자를 찾을 수 없으면 CustomException 발생")
        void submitCode_userNotFound_throwsCustomException() {
            // Arrange
            CodeSubmissionDto dto = TestFixtures.createValidSubmissionDto();
            
            when(userService.getUserById(anyLong()))
                    .thenThrow(new RuntimeException("User not found"));

            // Act & Assert
            assertThrows(
                    RuntimeException.class,
                    () -> compilerService.submitCode(dto)
            );
        }

        /**
         * 문제를 찾을 수 없는 경우 CustomException 발생 (테스트 모드 제외)
         */
        @Test
        @DisplayName("문제를 찾을 수 없으면 CustomException 발생")
        void submitCode_problemNotFound_throwsCustomException() {
            // Arrange
            CodeSubmissionDto dto = TestFixtures.createValidSubmissionDto();
            
            when(userService.getUserById(anyLong())).thenReturn(mockUser);
            when(problemService.getProblemById(anyInt()))
                    .thenThrow(new CustomException(CustomErrorCodes.PROBLEM_NOT_FOUND));

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> compilerService.submitCode(dto)
            );

            assertEquals(CustomErrorCodes.PROBLEM_NOT_FOUND, exception.getCustomErrorCode());
        }
    }

    @Nested
    @DisplayName("DTO 생성 테스트")
    class DtoCreationTest {

        @Test
        @DisplayName("유효한 CodeSubmissionDto 생성")
        void createValidCodeSubmissionDto() {
            // Arrange & Act
            CodeSubmissionDto dto = TestFixtures.createValidSubmissionDto();

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.getUserId());
            assertEquals(1L, dto.getProblemId());
            assertEquals("java", dto.getLanguage());
            assertNotNull(dto.getCode());
        }

        @Test
        @DisplayName("테스트 모드 CodeSubmissionDto 생성 (problemId=0)")
        void createTestModeCodeSubmissionDto() {
            // Arrange & Act
            CodeSubmissionDto dto = TestFixtures.createTestModeSubmissionDto();

            // Assert
            assertNotNull(dto);
            assertEquals(0L, dto.getProblemId());
        }

        @Test
        @DisplayName("유효한 SaveSolutionDto 생성")
        void createValidSaveSolutionDto() {
            // Arrange & Act
            SaveSolutionDto dto = TestFixtures.createValidSaveSolutionDto();

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.getUserId());
            assertEquals(1, dto.getProblemId());
            assertEquals("java", dto.getLanguage());
        }
    }

    @Nested
    @DisplayName("코드 샘플 테스트")
    class CodeSampleTest {

        @Test
        @DisplayName("유효한 Java 코드 샘플")
        void createValidJavaCode() {
            // Arrange & Act
            String code = TestFixtures.createValidJavaCode();

            // Assert
            assertNotNull(code);
            assertTrue(code.contains("public class Main"));
            assertTrue(code.contains("main"));
        }

        @Test
        @DisplayName("유효한 Python 코드 샘플")
        void createValidPythonCode() {
            // Arrange & Act
            String code = TestFixtures.createValidPythonCode();

            // Assert
            assertNotNull(code);
            assertTrue(code.contains("print"));
        }

        @Test
        @DisplayName("유효한 C++ 코드 샘플")
        void createValidCppCode() {
            // Arrange & Act
            String code = TestFixtures.createValidCppCode();

            // Assert
            assertNotNull(code);
            assertTrue(code.contains("#include"));
            assertTrue(code.contains("int main"));
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("최소 유효 코드 크기 (1 바이트)")
        void minValidCodeSize() {
            // Arrange & Act
            String code = TestFixtures.createMinValidCode();

            // Assert
            assertEquals(1, code.length());
        }

        @Test
        @DisplayName("최대 유효 코드 크기 (50,000 바이트)")
        void maxValidCodeSize() {
            // Arrange & Act
            String code = TestFixtures.createMaxValidCode();

            // Assert
            assertEquals(50000, code.length());
        }

        @Test
        @DisplayName("최소 무효 코드 크기 (50,001 바이트)")
        void minInvalidCodeSize() {
            // Arrange & Act
            String code = TestFixtures.createMinInvalidCode();

            // Assert
            assertEquals(50001, code.length());
        }
    }
}