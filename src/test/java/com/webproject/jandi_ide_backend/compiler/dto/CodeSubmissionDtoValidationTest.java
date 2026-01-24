package com.webproject.jandi_ide_backend.compiler.dto;

import com.webproject.jandi_ide_backend.common.TestFixtures;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeSubmissionDto 입력 검증 테스트
 * 
 * 블랙박스 테스트 방식으로 동등 분할 기법을 적용한다.
 * 각 필드의 유효/무효 동등 클래스와 경계값을 테스트한다.
 */
@DisplayName("CodeSubmissionDto 검증 테스트")
class CodeSubmissionDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("code 필드 테스트")
    class CodeFieldTest {

        /**
         * 동등 클래스 EC1: null
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("code가 null이면 검증 실패")
        void code_null_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code(null)
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("code")));
        }

        /**
         * 동등 클래스 EC2: 빈 문자열 ("")
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("code가 빈 문자열이면 검증 실패")
        void code_emptyString_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("")
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("code")));
        }

        /**
         * 동등 클래스 EC3: 공백만 있는 문자열
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("code가 공백만 있으면 검증 실패")
        void code_whitespaceOnly_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("   ")
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("code")));
        }

        /**
         * 동등 클래스 EC4: 유효 범위 (경계값 - 최소: 1 바이트)
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("code가 1바이트(최소 유효)이면 검증 성공")
        void code_minValidSize_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code(TestFixtures.createMinValidCode())
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("code")));
        }

        /**
         * 동등 클래스 EC4: 유효 범위 (경계값 - 최대: 50,000 바이트)
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("code가 50,000바이트(최대 유효)이면 검증 성공")
        void code_maxValidSize_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code(TestFixtures.createMaxValidCode())
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("code")));
        }

        /**
         * 동등 클래스 EC5: 최대 크기 초과 (50,001 바이트)
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("code가 50,001바이트(최소 무효)이면 검증 실패")
        void code_exceedsMaxSize_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code(TestFixtures.createMinInvalidCode())
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("code")));
        }
    }

    @Nested
    @DisplayName("language 필드 테스트")
    class LanguageFieldTest {

        /**
         * 동등 클래스 EC1: null
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("language가 null이면 검증 실패")
        void language_null_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language(null)
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("language")));
        }

        /**
         * 동등 클래스 EC2: 유효값 "java"
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("language가 'java'이면 검증 성공")
        void language_java_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("language")));
        }

        /**
         * 동등 클래스 EC3: 유효값 "python"
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("language가 'python'이면 검증 성공")
        void language_python_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("python")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("language")));
        }

        /**
         * 동등 클래스 EC4: 유효값 "c++"
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("language가 'c++'이면 검증 성공")
        void language_cpp_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("c++")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("language")));
        }

        /**
         * 동등 클래스 EC5: 지원하지 않는 언어
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("language가 지원하지 않는 언어면 검증 실패")
        void language_unsupported_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("javascript")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("language")));
        }

        /**
         * 동등 클래스 EC6: 대소문자 변형 "JAVA"
         * 기대 결과: 검증 성공 (대문자도 허용됨)
         */
        @Test
        @DisplayName("language가 대문자 'JAVA'이면 검증 성공")
        void language_uppercase_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("JAVA")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert - 대문자 'JAVA'도 허용되므로 검증 성공
            assertTrue(violations.isEmpty() || 
                    violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("language")));
        }
    }

    @Nested
    @DisplayName("userId 필드 테스트")
    class UserIdFieldTest {

        /**
         * 동등 클래스 EC1: null
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("userId가 null이면 검증 실패")
        void userId_null_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(null)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
        }

        /**
         * 동등 클래스 EC2: 양수
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("userId가 양수이면 검증 성공")
        void userId_positive_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("userId")));
        }

        /**
         * 동등 클래스 EC3: 0
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("userId가 0이면 검증 실패")
        void userId_zero_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(0L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
        }

        /**
         * 동등 클래스 EC4: 음수
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("userId가 음수이면 검증 실패")
        void userId_negative_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(-1L)
                    .problemId(1L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
        }
    }

    @Nested
    @DisplayName("problemId 필드 테스트")
    class ProblemIdFieldTest {

        /**
         * 동등 클래스 EC1: null
         * 기대 결과: 검증 실패
         */
        @Test
        @DisplayName("problemId가 null이면 검증 실패")
        void problemId_null_validationFails() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(1L)
                    .problemId(null)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("problemId")));
        }

        /**
         * 동등 클래스 EC2: 0 (테스트 모드)
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("problemId가 0(테스트 모드)이면 검증 성공")
        void problemId_zero_testMode_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(1L)
                    .problemId(0L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("problemId")));
        }

        /**
         * 동등 클래스 EC3: 양수
         * 기대 결과: 검증 성공
         */
        @Test
        @DisplayName("problemId가 양수이면 검증 성공")
        void problemId_positive_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = CodeSubmissionDto.builder()
                    .code("code")
                    .language("java")
                    .userId(1L)
                    .problemId(100L)
                    .build();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("problemId")));
        }
    }

    @Nested
    @DisplayName("전체 DTO 유효성 테스트")
    class FullDtoValidationTest {

        @Test
        @DisplayName("모든 필드가 유효하면 검증 성공")
        void allFieldsValid_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = TestFixtures.createValidSubmissionDto();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("테스트 모드 DTO가 유효하면 검증 성공")
        void testModeDto_validationPasses() {
            // Arrange
            CodeSubmissionDto dto = TestFixtures.createTestModeSubmissionDto();

            // Act
            Set<ConstraintViolation<CodeSubmissionDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
        }
    }
}
