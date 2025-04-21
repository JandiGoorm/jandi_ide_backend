package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import com.webproject.jandi_ide_backend.algorithm.solution.service.SolutionService;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilerService {

    private final JavaCompiler javaCompiler;
    private final PythonCompiler pythonCompiler;
    private final CppCompiler cppCompiler;
    private final ProblemService problemService;
    private final TestCaseService testCaseService;
    private final UserService userService;
    private final SolutionService solutionService;

    /**
     * 사용자 코드를 제출받아 컴파일 및 실행 후 결과를 저장합니다.
     * @param submissionDto 코드 제출 정보
     * @return 저장된 Solution 엔티티
     */
    @Transactional
    public Solution submitCode(CodeSubmissionDto submissionDto) {
        // 1. 필요한 정보 조회
        User user = userService.getUserById(submissionDto.getUserId());
        Problem problem = problemService.getProblemById(submissionDto.getProblemId());
        List<TestCase> testCases = testCaseService.getTestCasesByProblemId(problem.getId());
        
        // 2. 언어별 컴파일러 선택 및 실행
        List<ResultDto> results = compileAndRun(problem, testCases, submissionDto.getCode(), submissionDto.getLanguage());
        
        // 3. 결과 분석
        boolean isAllPass = results.stream().allMatch(result -> result.getStatus() == ResultStatus.PASS);
        
        // 4. 최대 실행 시간과 메모리 사용량 계산
        Double maxExecutionTime = results.stream()
                .mapToDouble(ResultDto::getExecutionTime)
                .max()
                .orElse(0.0);
        
        Double maxMemoryUsage = results.stream()
                .mapToDouble(ResultDto::getUsedMemory)
                .max()
                .orElse(0.0);
        
        // 5. 실행 결과 문자열 생성
        StringBuilder resultDetails = new StringBuilder();
        for (ResultDto result : results) {
            resultDetails.append("테스트 ").append(result.getTestNum()).append(": ")
                    .append(result.getStatus()).append("\n")
                    .append("실행 시간: ").append(result.getExecutionTime()).append("ms\n")
                    .append("메모리 사용량: ").append(result.getUsedMemory()).append("MB\n\n");
        }
        
        // 6. Solution 객체 생성 및 저장
        Solution solution = new Solution();
        solution.setUser(user);
        solution.setProblemId(problem.getId());
        solution.setCode(submissionDto.getCode());
        solution.setLanguage(submissionDto.getLanguage());
        solution.setSolvingTime(submissionDto.getSolvingTime());
        solution.setIsCorrect(isAllPass);
        solution.setAdditionalInfo(resultDetails.toString());
        solution.setMemoryUsage(maxMemoryUsage.intValue());
        solution.setExecutionTime(maxExecutionTime.intValue());
        
        // 7. 실행 결과에 따른 상태 설정
        if (isAllPass) {
            solution.setStatus(SolutionStatus.CORRECT);
        } else {
            if (hasCompilationError(results)) {
                solution.setStatus(SolutionStatus.COMPILATION_ERROR);
            } else if (hasRuntimeError(results)) {
                solution.setStatus(SolutionStatus.RUNTIME_ERROR);
            } else if (hasTimeoutError(results)) {
                solution.setStatus(SolutionStatus.TIMEOUT);
            } else if (hasMemoryLimitError(results)) {
                solution.setStatus(SolutionStatus.MEMORY_LIMIT);
            } else {
                solution.setStatus(SolutionStatus.WRONG_ANSWER);
            }
        }
        
        // 8. 솔루션 저장 및 반환
        return solutionService.saveSolution(solution);
    }
    
    /**
     * 언어별 컴파일러를 선택하여 코드를 컴파일하고 실행합니다.
     */
    private List<ResultDto> compileAndRun(Problem problem, List<TestCase> testCases, String code, String language) {
        return switch (language.toLowerCase()) {
            case "java" -> javaCompiler.runCode(problem, testCases, code);
            case "python" -> pythonCompiler.runCode(problem, testCases, code);
            case "c++" -> cppCompiler.runCode(problem, testCases, code);
            default -> throw new IllegalArgumentException("지원하지 않는 언어입니다: " + language);
        };
    }
    
    private boolean hasCompilationError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getStatus() == ResultStatus.ERROR && 
                result.getActualResult().contains("ERROR"));
    }
    
    private boolean hasRuntimeError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getStatus() == ResultStatus.ERROR && 
                !result.getActualResult().contains("메모리 초과") && 
                !result.getActualResult().contains("시간 초과"));
    }
    
    private boolean hasTimeoutError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getActualResult().contains("시간 초과"));
    }
    
    private boolean hasMemoryLimitError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getActualResult().contains("메모리 초과"));
    }
} 