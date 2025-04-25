package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.problem.service.ProblemService;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import com.webproject.jandi_ide_backend.algorithm.solution.service.SolutionService;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.algorithm.testCase.service.TestCaseService;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompileResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import com.webproject.jandi_ide_backend.compiler.dto.SaveSolutionDto;
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.user.entity.User;
import com.webproject.jandi_ide_backend.user.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 코드 컴파일 및 실행 서비스
 * 
 * 사용자가 제출한 코드를 컴파일하고 실행하여 결과를 반환하는 서비스입니다.
 * Java, Python, C++ 언어를 지원하며, 알고리즘 문제 풀이 및 단순 코드 테스트 기능을 제공합니다.
 */
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
    private final CompilerFileManager fileManager;

    /**
     * 컴파일러 서비스 생성자
     * 
     * @param javaCompiler Java 컴파일러
     * @param pythonCompiler Python 컴파일러
     * @param cppCompiler C++ 컴파일러
     * @param problemService 문제 서비스
     * @param testCaseService 테스트 케이스 서비스
     * @param userService 사용자 서비스
     * @param solutionService 솔루션 서비스
     * @param fileManager 컴파일러 파일 관리자
     */
    public CompilerService(
            JavaCompiler javaCompiler,
            PythonCompiler pythonCompiler,
            CppCompiler cppCompiler,
            ProblemService problemService,
            TestCaseService testCaseService,
            UserService userService,
            SolutionService solutionService,
            CompilerFileManager fileManager) {
        this.javaCompiler = javaCompiler;
        this.pythonCompiler = pythonCompiler;
        this.cppCompiler = cppCompiler;
        this.problemService = problemService;
        this.testCaseService = testCaseService;
        this.userService = userService;
        this.solutionService = solutionService;
        this.fileManager = fileManager;
    }
    
    /**
     * 애플리케이션 시작 시 컴파일러 작업 디렉토리 초기화
     */
    @PostConstruct
    public void init() {
        fileManager.initCompilerWorkspace();
    }

    /**
     * 사용자 코드를 제출받아 컴파일 및 실행 후 결과를 저장합니다.
     * 문제 ID가 0인 경우 테스트 케이스 없이 컴파일과 실행만 확인합니다.
     * 
     * @param submissionDto 코드 제출 정보 (사용자 ID, 문제 ID, 코드, 언어, 해결 시간)
     * @return 저장된 Solution 엔티티
     * @throws CompilerException 컴파일 또는 실행 중 오류 발생 시
     */
    @Transactional
    public Solution submitCode(CodeSubmissionDto submissionDto) {
        // 1. 필요한 정보 조회
        User user = userService.getUserById(submissionDto.getUserId());
        
        // 문제 ID가 0인 경우 특별 처리 (테스트 모드)
        if (submissionDto.getProblemId() == 0) {
            return handleSimpleCompilationCheck(user, submissionDto);
        }
        
        // 2. 일반적인 경우: 문제와 테스트 케이스 조회 및 실행
        Problem problem = problemService.getProblemById(submissionDto.getProblemId().intValue());
        List<TestCase> testCases = testCaseService.getTestCasesByProblemId(submissionDto.getProblemId().intValue());
        
        // 3. 언어별 컴파일러 선택 및 실행
        List<ResultDto> results = compileAndRun(problem, testCases, submissionDto.getCode(), 
                                             submissionDto.getLanguage(), submissionDto.getUserId());
        
        // 4. 결과 분석
        boolean isAllPass = results.stream().allMatch(result -> result.getStatus() == ResultStatus.CORRECT);
        
        // 5. 최대 실행 시간과 메모리 사용량 계산
        Double maxExecutionTime = 0.0;
        Double maxMemoryUsage = 0.0;

        if (results != null && !results.isEmpty()) {
            maxExecutionTime = results.stream()
                    .filter(result -> result != null)
                    .mapToDouble(result -> result.getExecutionTime() != null ? result.getExecutionTime() : 0.0)
                    .max()
                    .orElse(0.0);
            
            maxMemoryUsage = results.stream()
                    .filter(result -> result != null)
                    .mapToDouble(result -> result.getUsedMemory() != null ? result.getUsedMemory() : 0.0)
                    .max()
                    .orElse(0.0);
        }
        
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
        solution.setProblemSetId(submissionDto.getProblemSetId());
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
            // 오류 유형에 따른 예외 발생
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
     * 테스트 모드에서는 컴파일 오류, 런타임 오류 등의 상세 정보를 반환하여 사용자가 코드 디버깅에 활용할 수 있도록 합니다.
     * 
     * @param user 사용자 정보
     * @param submissionDto 코드 제출 정보
     * @return 저장된 Solution 엔티티
     * @throws CompilerException 컴파일 또는 실행 중 오류 발생 시
     */
    private Solution handleSimpleCompilationCheck(User user, CodeSubmissionDto submissionDto) {
        String code = submissionDto.getCode();
        String language = submissionDto.getLanguage();
        
        // 기본 실행을 위한 간단한 입력값 생성 - 콤마로 구분된 입력으로 변경
        String simpleInput = "1,2";
        
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
                    output.append("자바 코드 컴파일 시작...\n");
                    isCompiled = checkJavaCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("자바 컴파일 에러", status, output.toString(), code, language);
                    }
                    output.append("컴파일 성공. 실행 시작...\n\n");
                    isExecuted = checkJavaExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("자바 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                case "python":
                    output.append("파이썬 코드 실행 시작...\n");
                    isCompiled = true; // Python은 인터프리터 언어라 컴파일 단계가 없음
                    isExecuted = checkPythonExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("파이썬 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                case "c++":
                    output.append("C++ 코드 컴파일 시작...\n");
                    isCompiled = checkCppCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("C++ 컴파일 에러", status, output.toString(), code, language);
                    }
                    output.append("컴파일 성공! 실행 시작...\n\n");
                    isExecuted = checkCppExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("C++ 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                default:
                    output.append("지원하지 않는 언어입니다: ").append(language);
                    output.append("\n현재 지원 언어: java, python, c++");
                    throw new CompilerException("지원하지 않는 언어", SolutionStatus.COMPILATION_ERROR, 
                          "언어: " + language + "는 지원되지 않습니다. 지원 언어: java, python, c++", code, language);
            }
            
            // 상태 결정 - 모든 검사 통과 시 CORRECT
            status = SolutionStatus.CORRECT;
            output.append("\n테스트 완료: 코드가 정상적으로 실행되었습니다.\n");
            
        } catch (CompilerException e) {
            // 이미 적절한 CompilerException이 발생한 경우 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외 처리
            output.append("예상치 못한 오류: ").append(e.getMessage());
            throw new CompilerException("알 수 없는 오류", SolutionStatus.RUNTIME_ERROR, 
                                      e.getMessage(), code, language);
        }
        
        // Solution 객체 생성 및 저장
        Solution solution = new Solution();
        solution.setUser(user);
        solution.setProblemId(0); // 문제 ID를 0으로 저장 (테스트 모드)
        solution.setProblemSetId(submissionDto.getProblemSetId());
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
     * 
     * @param code 컴파일할 Java 코드
     * @param output 컴파일 결과 및 오류 메시지를 저장할 StringBuilder
     * @return 컴파일 성공 여부
     */
    private boolean checkJavaCompilation(String code, StringBuilder output) {
        File javaFile = null;
        try {
            // 임시 파일 생성
            javaFile = new File("Main.java");
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(code);
            }
            
            // 컴파일 프로세스 실행
            ProcessBuilder compilePb = new ProcessBuilder("javac", javaFile.getAbsolutePath());
            Process compileProcess = compilePb.start();
            
            // 프로세스 출력 수집
            String[] results = collectProcessOutput(compileProcess, 10); // 10초 타임아웃
            String stdOut = results[0];
            String stdErr = results[1];
            
            // 프로세스 종료 대기
            int exitCode = compileProcess.waitFor();
            
            // 컴파일 결과 확인 (종료 코드가 0이 아니면 컴파일 실패)
            if (exitCode != 0) {
                // 오류 출력이 있으면 추가
                if (!stdErr.isEmpty()) {
                    output.append(stdErr);
                }
                // 표준 출력도 있으면 추가 (일부 컴파일러는 표준 출력으로 오류 메시지를 출력할 수 있음)
                if (!stdOut.isEmpty()) {
                    output.append(stdOut);
                }
                
                // 둘 다 비어있으면 기본 메시지
                if (stdErr.isEmpty() && stdOut.isEmpty()) {
                    output.append("컴파일 실패 (종료 코드: ").append(exitCode).append(")");
                }
                
                return false;
            }
            
            // 성공이지만 출력이 있는 경우 (경고 등)
            if (!stdOut.isEmpty()) {
                output.append("컴파일러 출력: ").append(stdOut);
            }
            
            return true;
        } catch (TimeoutException e) {
            output.append("컴파일 시간 초과 (10초)");
            return false;
        } catch (Exception e) {
            output.append("컴파일 중 시스템 오류: ").append(e.getMessage());
            return false;
        } finally {
            // 임시 파일 정리
            if (javaFile != null) {
                javaFile.delete();
            }
            // 클래스 파일 삭제
            new File("Main.class").delete();
        }
    }
    
    /**
     * Java 코드 실행 여부 확인
     * 
     * @param code 실행할 Java 코드
     * @param input 표준 입력으로 전달할 데이터
     * @param output 실행 결과 및 오류 메시지를 저장할 StringBuilder
     * @return 실행 성공 여부
     */
    private boolean checkJavaExecution(String code, String input, StringBuilder output) {
        File javaFile = null;
        try {
            // 임시 파일 생성
            javaFile = new File("Main.java");
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(code);
            }
            
            // 실행 프로세스 시작
            ProcessBuilder runPb = new ProcessBuilder("java", "Main");
            Process runProcess = runPb.start();
            
            // 입력 데이터 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input);
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 프로세스 출력 수집 (5초 타임아웃)
            String[] results = collectProcessOutput(runProcess, 5);
            String stdOut = results[0];
            String stdErr = results[1];
            
            // 프로세스 종료 대기
            int exitCode = runProcess.waitFor();
            
            // 실행 결과 및 오류 기록
            output.append("실행 결과 (종료 코드: ").append(exitCode).append("):\n");
            
            if (!stdOut.isEmpty()) {
                output.append("표준 출력:\n").append(stdOut);
            } else {
                output.append("(표준 출력 없음)\n");
            }
            
            // 오류가 있는 경우 추가
            if (!stdErr.isEmpty()) {
                output.append("\n오류 출력:\n").append(stdErr);
                return false;
            }
            
            // 종료 코드가 0이 아니면 실행 실패로 간주
            if (exitCode != 0) {
                output.append("\n비정상 종료: 종료 코드 ").append(exitCode);
                return false;
            }
            
            return true;
        } catch (TimeoutException e) {
            output.append("실행 시간 초과 (5초)");
            return false;
        } catch (Exception e) {
            output.append("실행 중 시스템 오류: ").append(e.getMessage());
            return false;
        } finally {
            // 임시 파일 정리
            if (javaFile != null) {
                javaFile.delete();
            }
            // 클래스 파일 삭제
            new File("Main.class").delete();
        }
    }
    
    /**
     * Python 코드 실행 여부 확인
     * 
     * @param code 실행할 Python 코드
     * @param input 표준 입력으로 전달할 데이터
     * @param output 실행 결과 및 오류 메시지를 저장할 StringBuilder
     * @return 실행 성공 여부
     */
    private boolean checkPythonExecution(String code, String input, StringBuilder output) {
        File pythonFile = null;
        try {
            // 임시 파일 생성
            pythonFile = new File("Main.py");
            try (FileWriter writer = new FileWriter(pythonFile)) {
                writer.write(code);
            }
            
            // 여러 Python 인터프리터 명령어를 순차적으로 시도
            String[] pythonInterpreters = {"python3", "python", "py"};
            ProcessBuilder runPb = null;
            Process runProcess = null;
            boolean started = false;
            
            for (String interpreter : pythonInterpreters) {
                try {
                    runPb = new ProcessBuilder(interpreter, pythonFile.getAbsolutePath());
                    log.debug("Trying Python interpreter: {}", interpreter);
                    runProcess = runPb.start();
                    started = true;
                    log.debug("Successfully started Python with: {}", interpreter);
                    break;
                } catch (IOException e) {
                    log.warn("Failed to start Python with interpreter {}: {}", interpreter, e.getMessage());
                }
            }
            
            if (!started || runProcess == null) {
                throw new IOException("Unable to start any Python interpreter. Tried: " + 
                                      String.join(", ", pythonInterpreters));
            }
            
            // 입력 데이터 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input); // 직접 전체 입력을 한번에 전달
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 프로세스 출력 수집 (5초 타임아웃)
            String[] results = collectProcessOutput(runProcess, 5);
            String stdOut = results[0];
            String stdErr = results[1];
            
            // 프로세스 종료 대기
            int exitCode = runProcess.waitFor();
            
            // 실행 결과 및 오류 기록
            output.append("실행 결과 (종료 코드: ").append(exitCode).append("):\n");
            
            if (!stdOut.isEmpty()) {
                output.append("표준 출력:\n").append(stdOut);
            } else {
                output.append("(표준 출력 없음)\n");
            }
            
            // 오류가 있는 경우 추가
            if (!stdErr.isEmpty()) {
                output.append("\n오류 출력:\n").append(stdErr);
                return false;
            }
            
            // 종료 코드가 0이 아니면 실행 실패로 간주
            if (exitCode != 0) {
                output.append("\n비정상 종료: 종료 코드 ").append(exitCode);
                return false;
            }
            
            return true;
        } catch (TimeoutException e) {
            output.append("실행 시간 초과 (5초)");
            return false;
        } catch (Exception e) {
            output.append("실행 중 시스템 오류: ").append(e.getMessage());
            return false;
        } finally {
            // 임시 파일 정리
            if (pythonFile != null) {
                pythonFile.delete();
            }
        }
    }
    
    /**
     * C++ 코드 컴파일 여부 확인
     * 
     * @param code 컴파일할 C++ 코드
     * @param output 컴파일 결과 및 오류 메시지를 저장할 StringBuilder
     * @return 컴파일 성공 여부
     */
    private boolean checkCppCompilation(String code, StringBuilder output) {
        File cppFile = null;
        try {
            // 임시 파일 생성
            cppFile = new File("Main.cpp");
            try (FileWriter writer = new FileWriter(cppFile)) {
                writer.write(code);
            }
            
            // 컴파일 프로세스 실행
            ProcessBuilder compilePb = new ProcessBuilder("g++", cppFile.getAbsolutePath(), "-o", "Main");
            Process compileProcess = compilePb.start();
            
            // 프로세스 출력 수집
            String[] results = collectProcessOutput(compileProcess, 10); // 10초 타임아웃
            String stdOut = results[0];
            String stdErr = results[1];
            
            // 프로세스 종료 대기
            int exitCode = compileProcess.waitFor();
            
            // 컴파일 결과 확인 (종료 코드가 0이 아니면 컴파일 실패)
            if (exitCode != 0) {
                // 오류 출력이 있으면 추가
                if (!stdErr.isEmpty()) {
                    output.append(stdErr);
                }
                // 표준 출력도 있으면 추가 (일부 컴파일러는 표준 출력으로 오류 메시지를 출력할 수 있음)
                if (!stdOut.isEmpty()) {
                    output.append(stdOut);
                }
                
                // 둘 다 비어있으면 기본 메시지
                if (stdErr.isEmpty() && stdOut.isEmpty()) {
                    output.append("컴파일 실패 (종료 코드: ").append(exitCode).append(")");
                }
                
                return false;
            }
            
            // 성공이지만 출력이 있는 경우 (경고 등)
            if (!stdOut.isEmpty()) {
                output.append("컴파일러 출력: ").append(stdOut);
            }
            
            return true;
        } catch (TimeoutException e) {
            output.append("컴파일 시간 초과 (10초)");
            return false;
        } catch (Exception e) {
            output.append("컴파일 중 시스템 오류: ").append(e.getMessage());
            return false;
        } finally {
            // 임시 파일 정리
            if (cppFile != null) {
                cppFile.delete();
            }
        }
    }
    
    /**
     * C++ 코드 실행 여부 확인
     * 
     * @param code 실행할 C++ 코드
     * @param input 표준 입력으로 전달할 데이터
     * @param output 실행 결과 및 오류 메시지를 저장할 StringBuilder
     * @return 실행 성공 여부
     */
    private boolean checkCppExecution(String code, String input, StringBuilder output) {
        File cppFile = null;
        try {
            // 임시 파일 생성
            cppFile = new File("Main.cpp");
            try (FileWriter writer = new FileWriter(cppFile)) {
                writer.write(code);
            }
            
            // 실행 프로세스 시작
            ProcessBuilder runPb = new ProcessBuilder("./Main");
            Process runProcess = runPb.start();
            
            // 입력 데이터 전달
            if (input != null && !input.isEmpty()) {
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    processInput.write(input);
                    processInput.newLine();
                    processInput.flush();
                }
            }
            
            // 프로세스 출력 수집 (5초 타임아웃)
            String[] results = collectProcessOutput(runProcess, 5);
            String stdOut = results[0];
            String stdErr = results[1];
            
            // 프로세스 종료 대기
            int exitCode = runProcess.waitFor();
            
            // 실행 결과 및 오류 기록
            output.append("실행 결과 (종료 코드: ").append(exitCode).append("):\n");
            
            if (!stdOut.isEmpty()) {
                output.append("표준 출력:\n").append(stdOut);
            } else {
                output.append("(표준 출력 없음)\n");
            }
            
            // 오류가 있는 경우 추가
            if (!stdErr.isEmpty()) {
                output.append("\n오류 출력:\n").append(stdErr);
                return false;
            }
            
            // 종료 코드가 0이 아니면 실행 실패로 간주
            if (exitCode != 0) {
                output.append("\n비정상 종료: 종료 코드 ").append(exitCode);
                return false;
            }
            
            return true;
        } catch (TimeoutException e) {
            output.append("실행 시간 초과 (5초)");
            return false;
        } catch (Exception e) {
            output.append("실행 중 시스템 오류: ").append(e.getMessage());
            return false;
        } finally {
            // 임시 파일 정리
            if (cppFile != null) {
                cppFile.delete();
            }
            // 실행 파일 삭제
            new File("Main").delete();
        }
    }
    
    /**
     * 언어별 컴파일러를 선택하여 코드를 컴파일하고 실행합니다.
     * 
     * @param problem 문제 정보
     * @param testCases 테스트 케이스 목록
     * @param code 실행할 코드
     * @param language 프로그래밍 언어
     * @param userId 사용자 ID
     * @return 테스트 케이스별 실행 결과 목록
     * @throws IllegalArgumentException 지원하지 않는 언어인 경우
     */
    private List<ResultDto> compileAndRun(Problem problem, List<TestCase> testCases, String code, String language, Long userId) {
        return switch (language.toLowerCase()) {
            case "java" -> javaCompiler.runCode(problem, testCases, code, userId);
            case "python" -> pythonCompiler.runCode(problem, testCases, code, userId);
            case "c++" -> cppCompiler.runCode(problem, testCases, code, userId);
            default -> throw new IllegalArgumentException("지원하지 않는 언어입니다: " + language);
        };
    }
    
    /**
     * 컴파일 오류 여부 확인
     * 
     * @param results 테스트 결과 목록
     * @return 컴파일 오류 존재 여부
     */
    private boolean hasCompilationError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getStatus() == ResultStatus.COMPILATION_ERROR || 
                (result.getStatus() == ResultStatus.RUNTIME_ERROR && 
                 result.getActualResult().contains("ERROR")));
    }
    
    /**
     * 런타임 오류 여부 확인
     * 
     * @param results 테스트 결과 목록
     * @return 런타임 오류 존재 여부
     */
    private boolean hasRuntimeError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getStatus() == ResultStatus.RUNTIME_ERROR && 
                !result.getActualResult().contains("메모리 초과") && 
                !result.getActualResult().contains("시간 초과"));
    }
    
    /**
     * 시간 초과 오류 여부 확인
     * 
     * @param results 테스트 결과 목록
     * @return 시간 초과 오류 존재 여부
     */
    private boolean hasTimeoutError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getActualResult().contains("시간 초과"));
    }
    
    /**
     * 메모리 제한 초과 오류 여부 확인
     * 
     * @param results 테스트 결과 목록
     * @return 메모리 제한 초과 오류 존재 여부
     */
    private boolean hasMemoryLimitError(List<ResultDto> results) {
        return results.stream().anyMatch(result -> 
                result.getActualResult().contains("메모리 초과"));
    }
    
    /**
     * 오류 상세 정보 추출
     * 
     * @param results 테스트 결과 목록
     * @return 상세 오류 메시지
     */
    private String getErrorDetails(List<ResultDto> results) {
        return results.stream()
                .filter(result -> result.getStatus() == ResultStatus.RUNTIME_ERROR || 
                                  result.getStatus() == ResultStatus.COMPILATION_ERROR)
                .map(ResultDto::getActualResult)
                .collect(Collectors.joining("\n"));
    }
    
    /**
     * 오답 상세 정보 추출
     * 
     * @param results 테스트 결과 목록
     * @return 오답 상세 정보 (기대 출력과 실제 출력 비교)
     */
    private String getWrongAnswerDetails(List<ResultDto> results) {
        StringBuilder details = new StringBuilder();
        
        for (ResultDto result : results) {
            if (result.getStatus() == ResultStatus.WRONG_ANSWER) {
                details.append("테스트 케이스 ").append(result.getTestNum()).append(":\n")
                      .append("- 기대 출력:\n").append(result.getExpectedResult()).append("\n")
                      .append("- 실제 출력:\n").append(result.getActualResult()).append("\n");
            }
        }
        
        return details.toString();
    }

    /**
     * 코드를 컴파일하고 실행하여 결과만 반환합니다. 솔루션을 저장하지 않습니다.
     * 
     * @param submissionDto 코드 제출 정보
     * @return 컴파일 및 실행 결과
     */
    public CompileResultDto compileCode(CodeSubmissionDto submissionDto) {
        // 1. 필요한 정보 조회
        User user = userService.getUserById(submissionDto.getUserId());
        
        // 문제 ID가 0인 경우 특별 처리 (테스트 모드)
        if (submissionDto.getProblemId() == 0) {
            return handleSimpleCompilationTest(user, submissionDto);
        }
        
        // 2. 일반적인 경우: 문제와 테스트 케이스 조회 및 실행
        Problem problem = problemService.getProblemById(submissionDto.getProblemId().intValue());
        List<TestCase> testCases = testCaseService.getTestCasesByProblemId(submissionDto.getProblemId().intValue());
        
        // 3. 언어별 컴파일러 선택 및 실행
        List<ResultDto> results = compileAndRun(problem, testCases, submissionDto.getCode(), 
                                             submissionDto.getLanguage(), submissionDto.getUserId());
        
        // 4. 결과 분석
        boolean isAllPass = results.stream().allMatch(result -> result.getStatus() == ResultStatus.CORRECT);
        
        // 5. 최대 실행 시간과 메모리 사용량 계산
        Double maxExecutionTime = 0.0;
        Double maxMemoryUsage = 0.0;

        if (results != null && !results.isEmpty()) {
            maxExecutionTime = results.stream()
                    .filter(result -> result != null)
                    .mapToDouble(result -> result.getExecutionTime() != null ? result.getExecutionTime() : 0.0)
                    .max()
                    .orElse(0.0);
            
            maxMemoryUsage = results.stream()
                    .filter(result -> result != null)
                    .mapToDouble(result -> result.getUsedMemory() != null ? result.getUsedMemory() : 0.0)
                    .max()
                    .orElse(0.0);
        }
        
        // 6. 실행 결과 문자열 생성
        StringBuilder resultDetails = new StringBuilder();
        for (ResultDto result : results) {
            resultDetails.append("테스트 ").append(result.getTestNum()).append(": ")
                    .append(result.getStatus()).append("\n")
                    .append("실행 시간: ").append(result.getExecutionTime()).append("ms\n")
                    .append("메모리 사용량: ").append(result.getUsedMemory()).append("MB\n\n");
        }
        
        // 7. 결과 상태 설정
        SolutionStatus status;
        if (isAllPass) {
            status = SolutionStatus.CORRECT;
        } else {
            // 오류 유형에 따른 예외 발생
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
        
        // 8. 결과 반환
        return CompileResultDto.builder()
                .status(status)
                .isCorrect(isAllPass)
                .resultDetails(resultDetails.toString())
                .executionTime(maxExecutionTime.intValue())
                .memoryUsage(maxMemoryUsage.intValue())
                .testResults(results)
                .code(submissionDto.getCode())
                .language(submissionDto.getLanguage())
                .build();
    }
    
    /**
     * 테스트 모드에서 코드 컴파일 및 실행 결과만 반환합니다.
     */
    private CompileResultDto handleSimpleCompilationTest(User user, CodeSubmissionDto submissionDto) {
        String code = submissionDto.getCode();
        String language = submissionDto.getLanguage();
        
        // 기본 실행을 위한 간단한 입력값 생성 - 콤마로 구분된 입력으로 변경
        String simpleInput = "1,2";
        
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
                    output.append("자바 코드 컴파일 시작...\n");
                    isCompiled = checkJavaCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("자바 컴파일 에러", status, output.toString(), code, language);
                    }
                    output.append("컴파일 성공. 실행 시작...\n\n");
                    isExecuted = checkJavaExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("자바 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                case "python":
                    output.append("파이썬 코드 실행 시작...\n");
                    isCompiled = true; // Python은 인터프리터 언어라 컴파일 단계가 없음
                    isExecuted = checkPythonExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("파이썬 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                case "c++":
                    output.append("C++ 코드 컴파일 시작...\n");
                    isCompiled = checkCppCompilation(code, output);
                    if (!isCompiled) {
                        status = SolutionStatus.COMPILATION_ERROR;
                        throw new CompilerException("C++ 컴파일 에러", status, output.toString(), code, language);
                    }
                    output.append("컴파일 성공! 실행 시작...\n\n");
                    isExecuted = checkCppExecution(code, simpleInput, output);
                    if (!isExecuted) {
                        status = SolutionStatus.RUNTIME_ERROR;
                        throw new CompilerException("C++ 실행 오류", status, output.toString(), code, language);
                    }
                    break;
                    
                default:
                    output.append("지원하지 않는 언어입니다: ").append(language);
                    output.append("\n현재 지원 언어: java, python, c++");
                    throw new CompilerException("지원하지 않는 언어", SolutionStatus.COMPILATION_ERROR, 
                          "언어: " + language + "는 지원되지 않습니다. 지원 언어: java, python, c++", code, language);
            }
            
            // 상태 결정 - 모든 검사 통과 시 CORRECT
            status = SolutionStatus.CORRECT;
            output.append("\n테스트 완료: 코드가 정상적으로 실행되었습니다.\n");
            
        } catch (CompilerException e) {
            // 이미 적절한 CompilerException이 발생한 경우 그대로 전파
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외 처리
            output.append("예상치 못한 오류: ").append(e.getMessage());
            throw new CompilerException("알 수 없는 오류", SolutionStatus.RUNTIME_ERROR, 
                                      e.getMessage(), code, language);
        }
        
        // 결과 반환
        return CompileResultDto.builder()
                .status(status)
                .isCorrect(status == SolutionStatus.CORRECT)
                .resultDetails(output.toString())
                .executionTime(executionTime.intValue())
                .memoryUsage(memoryUsage.intValue())
                .code(code)
                .language(language)
                .build();
    }
    
    /**
     * Solution 객체를 생성하고 저장합니다.
     * 
     * @param saveSolutionDto Solution 저장 요청 정보
     * @return 저장된 Solution 객체
     */
    @Transactional
    public Solution saveSolution(SaveSolutionDto saveSolutionDto) {
        // 사용자 조회
        User user = userService.getUserById(saveSolutionDto.getUserId());
        
        // Solution 객체 생성
        Solution solution = new Solution();
        solution.setUser(user);
        solution.setProblemId(saveSolutionDto.getProblemId());
        solution.setProblemSetId(saveSolutionDto.getProblemSetId());
        solution.setCode(saveSolutionDto.getCode());
        solution.setLanguage(saveSolutionDto.getLanguage());
        solution.setSolvingTime(saveSolutionDto.getSolvingTime());
        solution.setIsCorrect(saveSolutionDto.getIsCorrect());
        solution.setAdditionalInfo(saveSolutionDto.getAdditionalInfo());
        solution.setMemoryUsage(saveSolutionDto.getMemoryUsage());
        solution.setExecutionTime(saveSolutionDto.getExecutionTime());
        solution.setStatus(saveSolutionDto.getStatus());
        solution.setDescription(saveSolutionDto.getDescription());
        
        // Solution 저장 및 반환
        return solutionService.saveSolution(solution);
    }

    /**
     * 코드가 비어있는지 검증하고, 비어있는 경우 적절한 에러 응답을 반환합니다.
     * 
     * @param code 검증할 코드
     * @param language 프로그래밍 언어
     * @return 비어있는 경우 CompilerErrorResponseDto, 그렇지 않으면 null
     */
    public CompilerErrorResponseDto validateCode(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            return CompilerErrorResponseDto.builder()
                .status(400)
                .error("Invalid Input")
                .message("코드는 비어있을 수 없습니다")
                .timestamp(LocalDateTime.now())
                .errorType("COMPILATION_ERROR")
                .errorDetails("코드가 비어있습니다. 코드를 입력해주세요.")
                .code("")
                .language(language)
                .build();
        }
        return null;
    }
    
    /**
     * CompilerException을 처리하여 적절한 에러 응답을 생성합니다.
     * 
     * @param e 발생한 CompilerException
     * @return 에러 응답 DTO
     */
    public CompilerErrorResponseDto handleCompilerException(CompilerException e) {
        // 여기서는 메시지를 가공하지 않고 원본 컴파일러 오류를 그대로 전달
        return CompilerErrorResponseDto.builder()
            .status(400)
            .error(e.getErrorType() != null ? e.getErrorType().name() : "Compilation Failed")
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .errorType(e.getErrorType() != null ? e.getErrorType().name() : "COMPILATION_ERROR")
            .errorDetails(e.getErrorDetails()) // 원본 컴파일러 오류 메시지
            .code(e.getCode())
            .language(e.getLanguage())
            .build();
    }
    
    /**
     * 일반 예외를 처리하여 적절한 서버 에러 응답을 생성합니다.
     * 
     * @param e 발생한 예외
     * @param code 실행된 코드
     * @param language 프로그래밍 언어
     * @param isCompile 컴파일 과정에서 발생한 예외인지 여부
     * @return 에러 응답 DTO
     */
    public CompilerErrorResponseDto handleGeneralException(Exception e, String code, String language, boolean isCompile) {
        String errorMessage = isCompile ? 
            "코드 컴파일 중 예상치 못한 오류가 발생했습니다" : 
            "솔루션 저장 중 예상치 못한 오류가 발생했습니다";
            
        log.error("Unexpected error: {}", e.getMessage(), e);
        
        return CompilerErrorResponseDto.builder()
            .status(500)
            .error("Internal Server Error")
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .errorType("SERVER_ERROR")
            .errorDetails(e.getMessage())
            .code(code)
            .language(language)
            .build();
    }
    
    /**
     * 코드 제출 DTO에서 compileCode를 호출하고, 발생 가능한 모든 예외를 처리합니다.
     * 
     * @param submissionDto 코드 제출 정보
     * @return 컴파일 결과 또는 에러 응답
     */
    public Object processCompileRequest(CodeSubmissionDto submissionDto) {
        try {
            // 코드 유효성 검증
            CompilerErrorResponseDto validationError = validateCode(submissionDto.getCode(), submissionDto.getLanguage());
            if (validationError != null) {
                return validationError;
            }
            
            // 코드 컴파일 및 실행
            return compileCode(submissionDto);
        } catch (CompilerException e) {
            return handleCompilerException(e);
        } catch (Exception e) {
            return handleGeneralException(e, submissionDto.getCode(), submissionDto.getLanguage(), true);
        }
    }
    
    /**
     * 솔루션 저장 DTO에서 saveSolution을 호출하고, 발생 가능한 모든 예외를 처리합니다.
     * 
     * @param saveSolutionDto 솔루션 저장 정보
     * @return 저장된 솔루션 또는 에러 응답
     */
    public Object processSaveSolutionRequest(SaveSolutionDto saveSolutionDto) {
        try {
            // 코드 유효성 검증
            CompilerErrorResponseDto validationError = validateCode(saveSolutionDto.getCode(), saveSolutionDto.getLanguage());
            if (validationError != null) {
                return validationError;
            }
            
            // 솔루션 저장
            return saveSolution(saveSolutionDto);
        } catch (CompilerException e) {
            return handleCompilerException(e);
        } catch (Exception e) {
            return handleGeneralException(e, saveSolutionDto.getCode(), saveSolutionDto.getLanguage(), false);
        }
    }

    /**
     * 프로세스의 표준 출력과 표준 에러를 모두 수집하는 유틸리티 메소드
     * 
     * @param process 실행된 프로세스
     * @param timeout 타임아웃 시간 (초)
     * @return 출력 및 에러 수집 결과 (String[] 배열로 [표준출력, 표준에러] 형태)
     * @throws TimeoutException 타임아웃 발생 시
     * @throws ExecutionException 실행 중 예외 발생 시
     * @throws InterruptedException 인터럽트 발생 시
     */
    private String[] collectProcessOutput(Process process, int timeout) 
            throws TimeoutException, ExecutionException, InterruptedException {
        
        // 표준 출력과 에러를 동시에 읽기 위한 스레드 풀
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // 표준 출력 읽기
        Future<String> stdOutFuture = executor.submit(() -> {
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            return result.toString();
        });
        
        // 표준 에러 읽기
        Future<String> stdErrFuture = executor.submit(() -> {
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            return result.toString();
        });
        
        try {
            // 주어진 타임아웃 내에 두 스트림의 결과 획득
            String stdOut = stdOutFuture.get(timeout, TimeUnit.SECONDS);
            String stdErr = stdErrFuture.get(timeout, TimeUnit.SECONDS);
            
            executor.shutdown();
            return new String[] { stdOut, stdErr };
        } catch (TimeoutException e) {
            // 타임아웃 발생 시 프로세스와 스레드 풀 강제 종료
            process.destroyForcibly();
            executor.shutdownNow();
            throw e;
        }
    }
} 