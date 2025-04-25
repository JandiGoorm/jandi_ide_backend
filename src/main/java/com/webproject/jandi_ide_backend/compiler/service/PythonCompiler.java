package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class PythonCompiler {

    private final CompilerFileManager fileManager;

    public PythonCompiler(CompilerFileManager fileManager) {
        this.fileManager = fileManager;
    }

    public List<ResultDto> runCode(Problem problem, List<TestCase> testcases, String code, Long userId) {
        List<ResultDto> results = new ArrayList<>();
        Path workingDir = null;

        try {
            // 작업 디렉토리 생성
            Long problemId = Long.valueOf(problem.getId()); // Convert Integer to Long
            workingDir = fileManager.createWorkingDir(userId, problemId);

            for (int i = 0; i < testcases.size(); i++) {
                String input = testcases.get(i).getInput();
                String expectedOutput = testcases.get(i).getOutput();
                StringBuilder output = new StringBuilder();
                Double time = 0.0;
                Double memory = 0.0;

                try {
                    // 임시 파일 생성
                    File pythonFile = fileManager.createPythonFile(workingDir, code);
                    log.debug("Python code written to file: {}", code);

                    // 여러 Python 인터프리터 명령어를 순차적으로 시도
                    String[] pythonInterpreters = {"python3", "python", "py"};
                    ProcessBuilder compilePb = null;
                    Process runProcess = null;
                    boolean started = false;
                    
                    for (String interpreter : pythonInterpreters) {
                        try {
                            compilePb = new ProcessBuilder(interpreter, pythonFile.getAbsolutePath());
                            compilePb.directory(workingDir.toFile()); // 작업 디렉토리 설정
                            compilePb.redirectErrorStream(true);
                            log.debug("Trying Python interpreter: {}", interpreter);
                            runProcess = compilePb.start();
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

                    // 테스트 케이스 입력 전달
                    OutputStream stdin = runProcess.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
                    
                    log.debug("Test case input: {}", input);
                    // 입력값 처리: 공백으로 구분된 입력을 쉼표로 구분된 형식으로 변환
                    String[] inputValues = input.trim().split("\\s+");
                    String formattedInput = String.join(",", inputValues);
                    log.debug("Formatted input for test case {}: {}", i+1, formattedInput);
                    
                    writer.write(formattedInput);
                    writer.newLine();
                    writer.flush();
                    writer.close(); // 입력 스트림을 닫아 프로세스가 더 이상 입력을 기다리지 않게 함

                    // 타이머 시작
                    long startTime = System.nanoTime();
                    long startMemory = getUsedMemory();
                    
                    // 출력 읽기 - 단순화된 방식
                    InputStream stdout = runProcess.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
                    StringBuilder result = new StringBuilder();
                    String line;
                    
                    try {
                        // 프로세스 완료 대기 (타임아웃 설정)
                        boolean completed = runProcess.waitFor(problem.getTimeLimit(), TimeUnit.SECONDS);
                        
                        if (!completed) {
                            // 타임아웃 발생
                            runProcess.destroyForcibly();
                            output.append("시간 초과 발생: 실행 시간이 제한(")
                                  .append(problem.getTimeLimit())
                                  .append("초)을 초과했습니다.\n");
                            
                            results.add(ResultDto.builder()
                                    .testNum(i+1)
                                    .input(input)
                                    .expectedResult(expectedOutput)
                                    .actualResult(output.toString())
                                    .status(ResultStatus.TIMEOUT)
                                    .build());
                            continue;
                        }
                        
                        // 출력 읽기
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        
                        // 추가로 남은 출력이 있는지 확인 (한 줄 이상일 경우)
                        int available = stdout.available();
                        if (available > 0) {
                            byte[] buffer = new byte[available];
                            stdout.read(buffer);
                            result.append(new String(buffer));
                        }
                        
                        // 종료 코드 확인
                        int exitCode = runProcess.exitValue();
                        if (exitCode != 0) {
                            log.warn("Process exited with non-zero code: " + exitCode);
                        }
                        
                    } catch (InterruptedException e) {
                        runProcess.destroyForcibly();
                        output.append("실행이 중단되었습니다: ").append(e.getMessage()).append("\n");
                        
                        results.add(ResultDto.builder()
                                .testNum(i+1)
                                .input(input)
                                .expectedResult(expectedOutput)
                                .actualResult(output.toString())
                                .status(ResultStatus.RUNTIME_ERROR)
                                .build());
                        continue;
                    } finally {
                        // 자원 정리
                        reader.close();
                        stdout.close();
                        runProcess.destroyForcibly();
                        
                        long endTime = System.nanoTime();
                        long endMemory = getUsedMemory();
                        
                        Double executionTime = (double) TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                        Double usedMemoryMB = (double) ((endMemory - startMemory) / (1024 * 1024));
                        
                        time = executionTime;
                        memory = usedMemoryMB;
                    }

                    // 결과 저장
                    String resultString = result.toString().trim();
                    log.debug("Raw output capture: [" + resultString + "]");
                    output.append(resultString);

                    // 통과 여부 확인
                    boolean isPass = compareOutput(resultString, expectedOutput);

                    ResultDto resultDto = ResultDto.builder()
                            .testNum(i + 1)
                            .input(input)
                            .expectedResult(expectedOutput)
                            .actualResult(resultString)
                            .executionTime(time)
                            .usedMemory(memory)
                            .status(isPass ? ResultStatus.CORRECT : ResultStatus.WRONG_ANSWER)
                            .build();

                    results.add(resultDto);

                    // 결과 메시지 추가
                    if (isPass) {
                        output.append("\n테스트 케이스 #").append(i + 1).append(" 통과!");
                    } else {
                        output.append("\n테스트 케이스 #").append(i + 1).append(" 실패");
                        output.append("\n기대 출력:\n").append(expectedOutput);
                        output.append("\n실제 출력:\n").append(resultString);
                    }
                } catch (Exception e) {
                    output.append("🚨ERROR : ").append(e.getMessage()).append("\n");
                    results.add(ResultDto.builder()
                            .testNum(i + 1)
                            .input(input)
                            .expectedResult(expectedOutput)
                            .actualResult(output.toString())
                            .status(ResultStatus.RUNTIME_ERROR)
                            .build());
                }
            }
        } catch (IOException e) {
            log.error("Error creating working directory: {}", e.getMessage(), e);
            results.add(ResultDto.builder()
                    .testNum(1)
                    .actualResult("작업 디렉토리 생성 오류: " + e.getMessage())
                    .status(ResultStatus.RUNTIME_ERROR)
                    .build());
        } finally {
            // 작업이 끝난 후 디렉토리 정리
            if (workingDir != null) {
                fileManager.cleanupDirectory(workingDir.toFile());
            }
        }
        return results;
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private boolean compareOutput(String actual, String expected) {
        log.debug("Comparing - Actual output: [" + actual + "]");
        log.debug("Comparing - Expected output: [" + expected + "]");
        
        // 실제 출력에서 실행 결과 부분만 추출
        String actualOutput = "";
        if (actual.contains("실행 결과:")) {
            // 실행 결과: 이후의 모든 내용 가져오기
            String[] parts = actual.split("실행 결과:\\s*\\n?");
            if (parts.length > 1) {
                actualOutput = parts[1].trim();
                // 첫 번째 줄만 가져오기 (줄바꿈이 있다면)
                if (actualOutput.contains("\n")) {
                    String[] lines = actualOutput.split("\\n");
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.isEmpty()) {
                            actualOutput = trimmed;
                            break;
                        }
                    }
                }
            }
        } else {
            actualOutput = actual.trim();
        }
        
        // 줄바꿈과 공백 모두 제거
        actualOutput = actualOutput.replaceAll("\\s+", "");
        
        // 기대 출력에서도 공백 모두 제거
        String expectedOutput = expected.trim().replaceAll("\\s+", "");
        
        log.debug("After cleanup - Actual: [" + actualOutput + "], Expected: [" + expectedOutput + "]");
        
        return actualOutput.equals(expectedOutput);
    }
}
