package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PythonCompiler {

    public List<ResultDto> runCode(Problem problem, List<TestCase> testcases, String code) {
        List<ResultDto> results = new ArrayList<>();

        for (int i = 0; i < testcases.size(); i++) {
            String input = testcases.get(i).getInput();
            String expectedOutput = testcases.get(i).getOutput();
            StringBuilder output = new StringBuilder();
            Double time = 0.0;
            Double memory = 0.0;

            try {
                // 임시 파일 생성
                File pythonFile = new File("Main.py");
                try (FileWriter writer = new FileWriter(pythonFile)) {
                    writer.write(code);
                }

                // 실행 파일 실행
                String pythonInterpreter = "python3";
                ProcessBuilder compilePb = new ProcessBuilder(pythonInterpreter, pythonFile.getAbsolutePath());
                Process runProcess = compilePb.start();

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

                // 타임아웃 설정 (default는 테스트케이스마다 2초)
                String result;
                try {
                    result = future.get(problem.getTimeLimit(), TimeUnit.SECONDS);

                } catch (TimeoutException e) {
                    runProcess.destroy();
                    future.cancel(true);  // Future 강제 취소
                    result = "⌛️[ 시간 초과 ]\n";
                    results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.FAIL).build());
                    break;

                } catch (ExecutionException e) {
                    if (e.getCause() instanceof OutOfMemoryError) {
                        runProcess.destroy();
                        future.cancel(true);  // Future 강제 취소
                        result = "🚫[ 메모리 초과 ]\n";
                        results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.FAIL).build());
                        break;

                    } else {
                        runProcess.destroy();
                        result = "🚨[ 오류 ]\n";
                        results.add(ResultDto.builder().testNum(i+1).actualResult(result).status(ResultStatus.ERROR).build());
                        break;
                    }

                } finally {
                    long endTime = System.nanoTime();
                    long endMemory = getUsedMemory();

                    executor.shutdown();
                    pythonFile.delete();

                    Double executionTime = (double) TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    long usedMemory = (endMemory - startMemory) / (1024 * 1024);

                    time = executionTime;
                    memory = (double) usedMemory;

                    // 메모리 초과 검사
                    if (usedMemory > problem.getMemory() * 1024 * 1024) {
                        output.append("🚫[ 메모리 초과 ]\n");
                        results.add(ResultDto.builder().testNum(i+1).actualResult(output.toString()).status(ResultStatus.FAIL).build());
                        break;
                    }
                }

                output.append(result);

            } catch (Exception e) {
                output.append("🚨ERROR : ").append(e.getMessage()).append("\n");
            }

            // 통과 여부
            boolean isPass = compareOutput(output.toString(), expectedOutput);

            ResultDto result = ResultDto.builder()
                    .testNum(i + 1)
                    .input(input)
                    .expectedResult(expectedOutput)
                    .actualResult(output.toString())
                    .executionTime(time)
                    .usedMemory(memory)
                    .status(isPass ? ResultStatus.PASS : ResultStatus.FAIL)
                    .build();

            results.add(result);
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
