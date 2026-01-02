package com.webproject.jandi_ide_backend.algorithm.problem.service;

import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemDetailResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.dto.ProblemPageResponseDTO;
import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.repository.ProblemRepository;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.repository.TestCaseRepository;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import com.webproject.jandi_ide_backend.common.TestFixtures;
import com.webproject.jandi_ide_backend.global.error.CustomErrorCodes;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProblemService 단위 테스트
 * 
 * 문제 조회 기능을 블랙박스 테스트 방식으로 검증한다.
 * 동등 분할 기법을 적용하여 페이지네이션 및 정렬 기능을 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemService 테스트")
class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private TestCaseRepository testCaseRepository;

    @Mock
    private TestCaseService testCaseService;

    @InjectMocks
    private ProblemService problemService;

    private Problem mockProblem;
    private List<Problem> mockProblems;

    @BeforeEach
    void setUp() {
        mockProblem = TestFixtures.createMockProblem();
        
        Problem problem1 = TestFixtures.createMockProblemWithLevel(1);
        problem1.setId(1);
        problem1.setTitle("쉬운 문제");
        
        Problem problem2 = TestFixtures.createMockProblemWithLevel(3);
        problem2.setId(2);
        problem2.setTitle("중간 문제");
        
        Problem problem3 = TestFixtures.createMockProblemWithLevel(5);
        problem3.setId(3);
        problem3.setTitle("어려운 문제");
        
        mockProblems = Arrays.asList(problem1, problem2, problem3);
    }

    @Nested
    @DisplayName("getProblems 메서드 테스트")
    class GetProblemsTest {

        /**
         * 기본 페이지네이션 조회
         */
        @Test
        @DisplayName("기본 파라미터로 조회 시 페이지네이션된 결과 반환")
        void getProblems_defaultParams_returnsPaginatedResult() {
            // Arrange
            Page<Problem> problemPage = new PageImpl<>(mockProblems);
            when(problemRepository.count()).thenReturn(3L);
            when(problemRepository.findAll(any(Pageable.class))).thenReturn(problemPage);

            // Act
            ProblemPageResponseDTO result = problemService.getProblems(0, 10, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getData().size());
        }

        /**
         * 난이도 오름차순 정렬 (쉬운 문제부터)
         */
        @Test
        @DisplayName("난이도 오름차순 정렬 시 쉬운 문제부터 반환")
        void getProblems_sortByLevelAsc_returnsAscendingOrder() {
            // Arrange
            Page<Problem> problemPage = new PageImpl<>(mockProblems);
            when(problemRepository.count()).thenReturn(3L);
            when(problemRepository.findAllByOrderByLevelAsc(any(Pageable.class))).thenReturn(problemPage);

            // Act
            ProblemPageResponseDTO result = problemService.getProblems(0, 10, "level", "asc");

            // Assert
            assertNotNull(result);
            verify(problemRepository).findAllByOrderByLevelAsc(any(Pageable.class));
        }

        /**
         * 난이도 내림차순 정렬 (어려운 문제부터)
         */
        @Test
        @DisplayName("난이도 내림차순 정렬 시 어려운 문제부터 반환")
        void getProblems_sortByLevelDesc_returnsDescendingOrder() {
            // Arrange
            Page<Problem> problemPage = new PageImpl<>(mockProblems);
            when(problemRepository.count()).thenReturn(3L);
            when(problemRepository.findAllByOrderByLevelDesc(any(Pageable.class))).thenReturn(problemPage);

            // Act
            ProblemPageResponseDTO result = problemService.getProblems(0, 10, "level", "desc");

            // Assert
            assertNotNull(result);
            verify(problemRepository).findAllByOrderByLevelDesc(any(Pageable.class));
        }

        /**
         * 유효하지 않은 페이지 테스트 - 음수 페이지
         */
        @Test
        @DisplayName("음수 페이지로 조회 시 예외 발생")
        void getProblems_negativePage_throwsException() {
            // Arrange
            when(problemRepository.count()).thenReturn(3L);

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> problemService.getProblems(-1, 10, null, null)
            );
            assertEquals(CustomErrorCodes.INVALID_PAGE, exception.getCustomErrorCode());
        }

        /**
         * 유효하지 않은 페이지 테스트 - 범위 초과 페이지
         */
        @Test
        @DisplayName("범위를 초과한 페이지로 조회 시 예외 발생")
        void getProblems_outOfRangePage_throwsException() {
            // Arrange
            when(problemRepository.count()).thenReturn(3L);

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> problemService.getProblems(100, 10, null, null)
            );
            assertEquals(CustomErrorCodes.INVALID_PAGE, exception.getCustomErrorCode());
        }

        /**
         * 페이지 크기 경계값 테스트 - 크기 1
         */
        @Test
        @DisplayName("페이지 크기 1로 조회 시 1개만 반환")
        void getProblems_sizeOne_returnsOneItem() {
            // Arrange
            Page<Problem> problemPage = new PageImpl<>(mockProblems.subList(0, 1));
            when(problemRepository.count()).thenReturn(3L);
            when(problemRepository.findAll(any(Pageable.class))).thenReturn(problemPage);

            // Act
            ProblemPageResponseDTO result = problemService.getProblems(0, 1, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getSize());
        }
    }

    @Nested
    @DisplayName("getProblemById 메서드 테스트")
    class GetProblemByIdTest {

        /**
         * 존재하는 ID로 조회 성공
         */
        @Test
        @DisplayName("존재하는 ID로 조회 시 Problem 반환")
        void getProblemById_existingId_returnsProblem() {
            // Arrange
            when(problemRepository.findById(1)).thenReturn(Optional.of(mockProblem));

            // Act
            Problem result = problemService.getProblemById(1);

            // Assert
            assertNotNull(result);
            assertEquals(mockProblem.getId(), result.getId());
            assertEquals(mockProblem.getTitle(), result.getTitle());
        }

        /**
         * 존재하지 않는 ID로 조회 시 예외 발생
         */
        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 CustomException 발생")
        void getProblemById_nonExistingId_throwsException() {
            // Arrange
            when(problemRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> problemService.getProblemById(999)
            );

            assertEquals(CustomErrorCodes.PROBLEM_NOT_FOUND, exception.getCustomErrorCode());
        }

        /**
         * 경계값 테스트 - ID 1 (최소 양수)
         */
        @Test
        @DisplayName("ID 1(최소 양수)로 조회 시 Problem 반환")
        void getProblemById_idOne_returnsProblem() {
            // Arrange
            when(problemRepository.findById(1)).thenReturn(Optional.of(mockProblem));

            // Act
            Problem result = problemService.getProblemById(1);

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getProblemDetail 메서드 테스트")
    class GetProblemDetailTest {

        /**
         * 문제 상세 조회 성공
         */
        @Test
        @DisplayName("존재하는 ID로 상세 조회 시 테스트 케이스 포함 반환")
        void getProblemDetail_existingId_returnsDetailWithTestCases() {
            // Arrange
            List<TestCase> testCases = TestFixtures.createMockTestCases();
            when(problemRepository.findById(1)).thenReturn(Optional.of(mockProblem));
            when(testCaseRepository.findByProblemId(1)).thenReturn(testCases);

            // Act
            ProblemDetailResponseDTO result = problemService.getProblemDetail(1);

            // Assert
            assertNotNull(result);
            assertEquals(mockProblem.getId(), result.getId());
            assertEquals(mockProblem.getTitle(), result.getTitle());
        }

        /**
         * 존재하지 않는 ID로 상세 조회 시 예외 발생
         */
        @Test
        @DisplayName("존재하지 않는 ID로 상세 조회 시 CustomException 발생")
        void getProblemDetail_nonExistingId_throwsException() {
            // Arrange
            when(problemRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> problemService.getProblemDetail(999)
            );

            assertEquals(CustomErrorCodes.PROBLEM_NOT_FOUND, exception.getCustomErrorCode());
        }
    }

    @Nested
    @DisplayName("deleteProblem 메서드 테스트")
    class DeleteProblemTest {

        /**
         * 문제 삭제 성공
         */
        @Test
        @DisplayName("존재하는 ID로 삭제 시 성공")
        void deleteProblem_existingId_deletesSuccessfully() {
            // Arrange
            when(problemRepository.findById(1)).thenReturn(Optional.of(mockProblem));
            doNothing().when(problemRepository).delete(mockProblem);

            // Act & Assert
            assertDoesNotThrow(() -> problemService.deleteProblem(1));
            verify(problemRepository).delete(mockProblem);
        }

        /**
         * 존재하지 않는 문제 삭제 시 예외
         */
        @Test
        @DisplayName("존재하지 않는 ID로 삭제 시 CustomException 발생")
        void deleteProblem_nonExistingId_throwsException() {
            // Arrange
            when(problemRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            CustomException exception = assertThrows(
                    CustomException.class,
                    () -> problemService.deleteProblem(999)
            );

            assertEquals(CustomErrorCodes.PROBLEM_NOT_FOUND, exception.getCustomErrorCode());
        }
    }
}
