package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Service
@Slf4j
public class JavaCompiler {

    public JavaCompiler() {
        // 기본 생성자
    }

    public List<ResultDto> runCode(Problem problem, List<TestCase> testcases, String code) {
        List<ResultDto> results = new ArrayList<>();

        for (int i = 0; i < testcases.size(); i++) {
            String input = testcases.get(i).getInput();
            String expectedOutput = testcases.get(i).getOutput();
            StringBuilder output = new StringBuilder();
            Double time = 0.0;
            Double memory = 0.0;

            // 임시 파일 생성
            File javaFile = new File("Main.java");
            try (FileWriter writer = new FileWriter(javaFile)) {
                writer.write(code);
            } catch (IOException e) {
                output.append("🚨ERROR: ").append(e.getMessage()).append("\n");
                results.add(ResultDto.builder().testNum(i+1).actualResult(output.toString()).status(ResultStatus.COMPILATION_ERROR).build());
                continue;
            }

            // 컴파일. javaCompiler 경로 맞게 설정해야함.
            String javaCompiler = "javac";
            ProcessBuilder compilePb = new ProcessBuilder(javaCompiler, javaFile.getAbsolutePath());
            try {
                Process compileProcess = compilePb.start();
                compileProcess.waitFor();

                if (compileProcess.exitValue() != 0) {

                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))) {
                        String errorLine;

                        while ((errorLine = errorReader.readLine()) != null) {
                            output.append(errorLine).append("\n");
                        }
                    }
                    results.add(ResultDto.builder().testNum(i+1).actualResult(output.toString()).status(ResultStatus.COMPILATION_ERROR).build());
                    javaFile.delete();
                    continue;
                }
            } catch (IOException | InterruptedException e) {
                output.append("🚨ERROR: ").append(e.getMessage()).append("\n");
                results.add(ResultDto.builder().testNum(i+1).actualResult(output.toString()).status(ResultStatus.RUNTIME_ERROR).build());
                continue;
            }

            // 자바 파일 실행
            String javaRunner = "java";
            ProcessBuilder javaProcess = new ProcessBuilder(javaRunner, "-Xmx" + problem.getMemory() * 2 + "m", "Main");
            javaProcess.directory(javaFile.getParentFile()); // 클래스 파일이 있는 디렉토리 설정
            try {
                Process runProcess = javaProcess.start();

                // 테스트 케이스 입력 전달
                try (BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    String[] testcaseInputs = input.split("\\s+");
                    for (String inputLine : testcaseInputs) {
                        processInput.write(inputLine);
                        processInput.newLine();
                    }
                    processInput.flush();
                }

                // 타이머 시작 및 실행 결과 받아오기
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

                long startTime = System.nanoTime();
                long startMemory = getUsedMemory();

                // 타임아웃 설정 (default 는 테스트케이스마다 2초)
                String result;
                try {
                    result = future.get(problem.getTimeLimit(), TimeUnit.SECONDS);

                } catch (TimeoutException e) {
                    runProcess.destroy();
                    future.cancel(true);  // Future 강제 취소
                    result = "⌛️[ 시간 초과 ]\n";
                    results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.TIMEOUT).build());
                    break;

                } catch (ExecutionException e) {
                    if (e.getCause() instanceof OutOfMemoryError) {
                        runProcess.destroy();
                        future.cancel(true);  // Future 강제 취소
                        result = "🚫[ 메모리 초과 ]\n";
                        results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.MEMORY_LIMIT).build());
                        break;

                    } else {
                        runProcess.destroy();
                        result = "🚨[ 오류 ]\n";
                        results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.RUNTIME_ERROR).build());
                        break;
                    }

                } finally {
                    long endTime = System.nanoTime();
                    long endMemory = getUsedMemory();

                    executor.shutdown();
                    javaFile.delete();
                    new File(javaFile.getAbsolutePath().replace(".java", ".class")).delete();

                    Double executionTime = (double) TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    long usedMemory = (endMemory - startMemory) / (1024 * 1024);

                    time = executionTime;
                    memory = (double) usedMemory;

                    // 메모리 초과 검사
                    if (usedMemory > problem.getMemory() * 1024 * 1024) {
                        output.append("🚫[ 메모리 초과 ]\n");
                        results.add(ResultDto.builder().testNum(i+1).actualResult(output.toString()).status(ResultStatus.MEMORY_LIMIT).build());
                        break;
                    }
                }

                output.append(result);

            } catch (Exception e) {
                output.append("🚨ERROR: ").append(e.getMessage()).append("\n");
            }

            // 통과 여부
            // 아래의 코드는 결과값에 스페이스바가 들어가거나 엔터키가 하나 더 들어가는 등 양식에 조금의 오차가 생기면 FAIL이 되는 문제가 발생함.
            // 양식의 사소한 오차가 있을 때에도 FAIL 로 할 것이라면 주석친 코드를 사용하면 됌.
            boolean isPass = compareOutput(output.toString(), expectedOutput);

            results.add(ResultDto.builder()
                    .testNum(i + 1)
                    .input(input)
                    .expectedResult(expectedOutput)
                    .actualResult(output.toString())
                    .executionTime(time)
                    .usedMemory(memory)
                    .status(isPass ? ResultStatus.CORRECT : ResultStatus.WRONG_ANSWER)
                    .build());
        }
        return results;
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private boolean compareOutput(String actual, String expected) {
        String[] actualTokens = actual.trim().split("\\s+");
        String[] expectedTokens = expected.trim().split("\\s+");
        return Arrays.equals(actualTokens, expectedTokens);
    }
}
