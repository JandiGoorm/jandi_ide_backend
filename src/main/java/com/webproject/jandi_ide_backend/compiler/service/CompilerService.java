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
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompilerService {

    private final JavaCompiler javaCompiler;
    private final PythonCompiler pythonCompiler;
    private final CppCompiler cppCompiler;
    private final ProblemService problemService;
    private final TestCaseService testCaseService;
    private final UserService userService;
    private final SolutionService solutionService;

    public CompilerService(
            JavaCompiler javaCompiler,
            PythonCompiler pythonCompiler,
            CppCompiler cppCompiler,
            ProblemService problemService,
            TestCaseService testCaseService,
            UserService userService,
            SolutionService solutionService) {
        this.javaCompiler = javaCompiler;
        this.pythonCompiler = pythonCompiler;
        this.cppCompiler = cppCompiler;
        this.problemService = problemService;
        this.testCaseService = testCaseService;
        this.userService = userService;
        this.solutionService = solutionService;
    }

    /**
     * 사용자 코드를 제출받아 컴파일 및 실행 후 결과를 저장합니다.
     * 문제 ID가 0인 경우 테스트 케이스 없이 컴파일과 실행만 확인합니다.
     * @param submissionDto 코드 제출 정보
     * @return 저장된 Solution 엔티티
     */
    @Transactional
    public Solution submitCode(CodeSubmissionDto submissionDto) {
        // 1. 필요한 정보 조회
        User user = userService.getUserById(submissionDto.getUserId());
        
        // 문제 ID가 0인 경우 특별 처리
        if (submissionDto.getProblemId() == 0) {
            return handleSimpleCompilationCheck(user, submissionDto);
        }
        
        // 2. 일반적인 경우: 문제와 테스트 케이스 조회 및 실행
        Problem problem = problemService.getProblemById(submissionDto.getProblemId());
        List<TestCase> testCases = testCaseService.getTestCasesByProblemId(submissionDto.getProblemId());
        
        // 3. 언어별 컴파일러 선택 및 실행
        List<ResultDto> results = compileAndRun(problem, testCases, submissionDto.getCode(), submissionDto.getLanguage());
        
        // 4. 결과 분석
        boolean isAllPass = results.stream().allMatch(result -> result.getStatus() == ResultStatus.PASS);
        
        // 5. 최대 실행 시간과 메모리 사용량 계산
        Double maxExecutionTime = results.stream()
                .mapToDouble(ResultDto::getExecutionTime)
                .max()
                .orElse(0.0);
        
        Double maxMemoryUsage = results.stream()
                .mapToDouble(ResultDto::getUsedMemory)
                .max()
                .orElse(0.0);
        
        // 6. 실행 결과 문자열 생성
        StringBuilder resultDetails = new StringBuilder();
        for (ResultDto result : results) {
            resultDetails.append("테스트 ").append(result.getTestNum()).append(": ")
                    .append(result.getStatus()).append("\n")
                    .append("실행 시간: ").append(result.getExecutionTime()).append("ms\n")
                    .append("메모리 사용량: ").append(result.getUsedMemory()).append("MB\n\n");
        }
        
        // 7. Solution 객체 생성 및 저장
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
        
        // 8. 실행 결과에 따른 상태 설정
        SolutionStatus status;
        if (isAllPass) {
            status = SolutionStatus.CORRECT;
        } else {
            if (hasCompilationError(results)) {
                status = SolutionStatus.COMPILATION_ERROR;
                String errorDetails = getErrorDetails(results);
                throw new CompilerException("컴파일 에러가 발생했습니다", status, errorDetails, 
                        submissionDto.getCode(), submissionDto.getLanguage());
            } else if (hasRuntimeError(results)) {
                status = SolutionStatus.RUNTIME_ERROR;
                String errorDetails = getErrorDetails(results);
                throw new CompilerException("런타임 에러가 발생했습니다", status, errorDetails, 
                        submissionDto.getCode(), submissionDto.getLanguage());
            } else if (hasTimeoutError(results)) {
                status = SolutionStatus.TIMEOUT;
                throw new CompilerException("시간 초과가 발생했습니다", status, "실행 시간이 제한 시간을 초과했습니다", 
                        submissionDto.getCode(), submissionDto.getLanguage());
            } else if (hasMemoryLimitError(results)) {
                status = SolutionStatus.MEMORY_LIMIT;
                throw new CompilerException("메모리 초과가 발생했습니다", status, "프로그램이 메모리 제한을 초과했습니다", 
                        submissionDto.getCode(), submissionDto.getLanguage());
            } else {
                status = SolutionStatus.WRONG_ANSWER;
                String errorDetails = getWrongAnswerDetails(results);
                throw new CompilerException("틀린 답안입니다", status, errorDetails, 
                        submissionDto.getCode(), submissionDto.getLanguage());
            }
        }
        
        solution.setStatus(status);
        
        // 9. 솔루션 저장 및 반환
        return solutionService.saveSolution(solution);
    }
    
    /**
     * 문제 ID가 0인 경우 테스트 케이스 없이 단순 컴파일 및 실행 확인만 수행합니다.
     */
    private Solution handleSimpleCompilationCheck(User user, CodeSubmissionDto submissionDto) {
        String code = submissionDto.getCode();
        String language = submissionDto.getLanguage();
        
        // 기본 실행을 위한 간단한 입력값 생성
        String simpleInput = "10 20";
        
        // 결과를 저장할 객체
        StringBuilder output = new StringBuilder();
        Double executionTime = 0.0;
        Double memoryUsage = 0.0;
        boolean isCompiled = false;
        boolean isExecuted = false;
        SolutionStatus status = SolutionStatus.SUBMITTED;
        
        try {
            // 언어별 처리
            switch (language.toLowerCase()) {
                case "java":
                    isCompiled = checkJavaCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("자바 컴파일 에러가 발생했습니다", status, output.toString(), code, language);
                    }
                    isExecuted = checkJavaExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("자바 실행 오류가 발생했습니다", status, output.toString(), code, language);
                    }
                    break;
                    
                case "python":
                    isCompiled = true; // Python은 인터프리터 언어라 컴파일 단계가 없음
                    isExecuted = checkPythonExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("파이썬 실행 오류가 발생했습니다", status, output.toString(), code, language);
                    }
                    break;
                    
                case "c++":
                    isCompiled = checkCppCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("C++ 컴파일 에러가 발생했습니다", status, output.toString(), code, language);
                    }
                    isExecuted = checkCppExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("C++ 실행 오류가 발생했습니다", status, output.toString(), code, language);
                    }
                    break;
                    
                default:
                    output.append("🚨ERROR: 지원하지 않는 언어입니다: ").append(language);
                    throw new CompilerException("지원하지 않는 언어입니다", SolutionStatus.COMPILATION_ERROR, 
                          "언어: " + language + "는 지원되지 않습니다. 지원 언어: java, python, c++", code, language);
            }
            
            // 상태 결정
            status = SolutionStatus.CORRECT;
            
        } catch (CompilerException e) {
            // 이미 적절한 CompilerException이 발생한 경우 그대로 전파
            throw e;
        } catch (Exception e) {
            output.append("🚨ERROR: ").append(e.getMessage());
            throw new CompilerException("알 수 없는 오류가 발생했습니다", SolutionStatus.RUNTIME_ERROR, 
                                      e.getMessage(), code, language);
        }
        
        // Solution 객체 생성 및 반환
        Solution solution = new Solution();
        solution.setUser(user);
        solution.setProblemId(0); // 문제 ID를 0으로 저장
        solution.setCode(code);
        solution.setLanguage(language);
        solution.setSolvingTime(submissionDto.getSolvingTime());
        solution.setIsCorrect(status == SolutionStatus.CORRECT);
        solution.setAdditionalInfo(output.toString());
        solution.setMemoryUsage(memoryUsage.intValue());
        solution.setExecutionTime(executionTime.intValue());
        solution.setStatus(status);
        
        return solutionService.saveSolution(solution);
    }
    
    /**
     * Java 코드 컴파일 여부 확인
     */
    private boolean checkJavaCompilation(String code, StringBuilder output) {
        File javaFile = null;
        try {
            // 임시 파일 생성
            javaFile = new File("Main.java");
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(code);
            }
            
            // 컴파일
            ProcessBuilder compilePb = new ProcessBuilder("javac", javaFile.getAbsolutePath());
            Process compileProcess = compilePb.start();
            compileProcess.waitFor();
            
            // 컴파일 결과 확인
            if (compileProcess.exitValue() != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    output.append(errorLine).append("\n");
                }
                return false;
            }
            
            return true;
        } catch (Exception e) {
            output.append("컴파일 중 오류 발생: ").append(e.getMessage());
            return false;
        } finally {
            if (javaFile != null) {
                javaFile.delete();
            }
            // 클래스 파일 삭제
            new File("Main.class").delete();
        }
    }
    
    /**
     * Java 코드 실행 여부 확인
     */
    private boolean checkJavaExecution(String code, String input, StringBuilder output) {
        File javaFile = null;
        try {
            // 임시 파일 생성
            javaFile = new File("Main.java");
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(code);
            }
            
            // 실행
            ProcessBuilder runPb = new ProcessBuilder("java", "Main");
            Process runProcess = runPb.start();
            
            // 입력 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input);
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 결과 읽기
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
                return result.toString();
            });
            
            String result = future.get(5, TimeUnit.SECONDS); // 최대 5초 실행 시간 제한
            output.append("실행 결과: ").append(result);
            
            executor.shutdown();
            return true;
        } catch (Exception e) {
            output.append("실행 중 오류 발생: ").append(e.getMessage());
            return false;
        } finally {
            if (javaFile != null) {
                javaFile.delete();
            }
            // 클래스 파일 삭제
            new File("Main.class").delete();
        }
    }
    
    /**
     * Python 코드 실행 여부 확인
     */
    private boolean checkPythonExecution(String code, String input, StringBuilder output) {
        File pythonFile = null;
        try {
            // 임시 파일 생성
            pythonFile = new File("Main.py");
            try (FileWriter writer = new FileWriter(pythonFile)) {
                writer.write(code);
            }
            
            // 실행
            ProcessBuilder runPb = new ProcessBuilder("python3", pythonFile.getAbsolutePath());
            Process runProcess = runPb.start();
            
            // 입력 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input);
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 결과 읽기
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
                return result.toString();
            });
            
            String result = future.get(5, TimeUnit.SECONDS); // 최대 5초 실행 시간 제한
            output.append("실행 결과: ").append(result);
            
            executor.shutdown();
            return true;
        } catch (Exception e) {
            output.append("실행 중 오류 발생: ").append(e.getMessage());
            return false;
        } finally {
            if (pythonFile != null) {
                pythonFile.delete();
            }
        }
    }
    
    /**
     * C++ 코드 컴파일 여부 확인
     */
    private boolean checkCppCompilation(String code, StringBuilder output) {
        File cppFile = null;
        try {
            // 임시 파일 생성
            cppFile = new File("Main.cpp");
            try (FileWriter writer = new FileWriter(cppFile)) {
                writer.write(code);
            }
            
            // 컴파일
            ProcessBuilder compilePb = new ProcessBuilder("g++", cppFile.getAbsolutePath(), "-o", "Main");
            Process compileProcess = compilePb.start();
            compileProcess.waitFor();
            
            // 컴파일 결과 확인
            if (compileProcess.exitValue() != 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    output.append(errorLine).append("\n");
                }
                return false;
            }
            
            return true;
        } catch (Exception e) {
            output.append("컴파일 중 오류 발생: ").append(e.getMessage());
            return false;
        } finally {
            if (cppFile != null) {
                cppFile.delete();
            }
        }
    }
    
    /**
     * C++ 코드 실행 여부 확인
     */
    private boolean checkCppExecution(String code, String input, StringBuilder output) {
        File cppFile = null;
        try {
            // 임시 파일 생성
            cppFile = new File("Main.cpp");
            try (FileWriter writer = new FileWriter(cppFile)) {
                writer.write(code);
            }
            
            // 컴파일은 이미 완료되었다고 가정
            
            // 실행
            ProcessBuilder runPb = new ProcessBuilder("./Main");
            Process runProcess = runPb.start();
            
            // 입력 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input);
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 결과 읽기
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> future = executor.submit(() -> {
                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append("\n");
                    }
                }
                return result.toString();
            });
            
            String result = future.get(5, TimeUnit.SECONDS); // 최대 5초 실행 시간 제한
            output.append("실행 결과: ").append(result);
            
            executor.shutdown();
            return true;
        } catch (Exception e) {
            output.append("실행 중 오류 발생: ").append(e.getMessage());
            return false;
        } finally {
            if (cppFile != null) {
                cppFile.delete();
            }
            // 실행 파일 삭제
            new File("Main").delete();
        }
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
    
    private String getErrorDetails(List<ResultDto> results) {
        return results.stream()
                .filter(result -> result.getStatus() == ResultStatus.ERROR)
                .map(ResultDto::getActualResult)
                .collect(Collectors.joining("\n"));
    }
    
    private String getWrongAnswerDetails(List<ResultDto> results) {
        StringBuilder details = new StringBuilder();
        
        for (ResultDto result : results) {
            if (result.getStatus() == ResultStatus.FAIL) {
                details.append("테스트 케이스 ").append(result.getTestNum()).append(":\n")
                      .append("- 기대 출력: ").append(result.getExpectedResult()).append("\n")
                      .append("- 실제 출력: ").append(result.getActualResult()).append("\n");
            }
        }
        
        return details.toString();
    }
} 