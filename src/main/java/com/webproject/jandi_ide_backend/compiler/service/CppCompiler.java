package com.webproject.jandi_ide_backend.compiler.service;

import com.webproject.jandi_ide_backend.algorithm.problem.entity.Problem;
import com.webproject.jandi_ide_backend.algorithm.testCase.entity.TestCase;
import com.webproject.jandi_ide_backend.compiler.dto.ResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CppCompiler {

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
                File cppFile = new File("Main.cpp");
                try (FileWriter writer = new FileWriter(cppFile)) {
                    writer.write(code);
                }

                // 컴파일 및 Main 파일 생성
                String cppCompiler = "g++";
                ProcessBuilder compilePb = new ProcessBuilder(cppCompiler, cppFile.getAbsolutePath(), "-o", "Main");
                Process compileProcess = compilePb.start();
                compileProcess.waitFor();

                // 컴파일 에러 발생 시 에러 및 종료
                if (compileProcess.exitValue() != 0) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        output.append(errorLine).append("\n");
                    }
                    results.add(ResultDto.builder().actualResult(output.toString()).status(ResultStatus.ERROR).build());
                    cppFile.delete();
                    continue;
                }

                // 실행 파일 실행
                ProcessBuilder runPb = new ProcessBuilder("./Main");
                runPb.directory(cppFile.getParentFile()); // 실행 파일이 있는 디렉토리 설정
                Process runProcess = runPb.start();

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
                    cppFile.delete();
                    new File("Main").delete();

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
