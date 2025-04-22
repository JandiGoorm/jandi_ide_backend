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
                output.append("파일 생성 오류: ").append(e.getMessage()).append("\n");
                output.append("파일 시스템 권한이나 디스크 공간을 확인해 주세요.\n");
                results.add(ResultDto.builder()
                        .testNum(i+1)
                        .input(input)
                        .expectedResult(expectedOutput)
                        .actualResult(output.toString())
                        .status(ResultStatus.COMPILATION_ERROR)
                        .build());
                continue;
            }

            // 컴파일. javaCompiler 경로 맞게 설정해야함.
            String javaCompiler = "javac";
            ProcessBuilder compilePb = new ProcessBuilder(javaCompiler, javaFile.getAbsolutePath());
            try {
                Process compileProcess = compilePb.start();
                compileProcess.waitFor();

                if (compileProcess.exitValue() != 0) {
                    output.append("컴파일 에러 발생:\n");
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))) {
                        String errorLine;

                        while ((errorLine = errorReader.readLine()) != null) {
                            output.append(errorLine).append("\n");
                        }
                    }
                    output.append("\n일반적인 컴파일 오류 원인:\n");
                    output.append("  - 세미콜론(;) 누락\n");
                    output.append("  - 괄호 불일치\n");
                    output.append("  - 변수 또는 메소드 이름 오타\n");
                    output.append("  - 타입 불일치\n");
                    
                    results.add(ResultDto.builder()
                            .testNum(i+1)
                            .input(input)
                            .expectedResult(expectedOutput)
                            .actualResult(output.toString())
                            .status(ResultStatus.COMPILATION_ERROR)
                            .build());
                    javaFile.delete();
                    continue;
                }
            } catch (IOException | InterruptedException e) {
                output.append("컴파일 프로세스 오류: ").append(e.getMessage()).append("\n");
                output.append("JDK가 올바르게 설치되어 있는지 확인해 주세요.\n");
                results.add(ResultDto.builder()
                        .testNum(i+1)
                        .input(input)
                        .expectedResult(expectedOutput)
                        .actualResult(output.toString())
                        .status(ResultStatus.RUNTIME_ERROR)
                        .build());
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
                    output.append("시간 초과 발생: 실행 시간이 제한(")
                          .append(problem.getTimeLimit())
                          .append("초)을 초과했습니다.\n");
                    output.append("알고리즘의 복잡도를 개선하거나, 무한 루프를 확인해 보세요.\n");
                    
                    results.add(ResultDto.builder()
                            .testNum(i+1)
                            .input(input)
                            .expectedResult(expectedOutput)
                            .actualResult(output.toString())
                            .status(ResultStatus.TIMEOUT)
                            .build());
                    break;

                } catch (ExecutionException e) {
                    if (e.getCause() instanceof OutOfMemoryError) {
                        runProcess.destroy();
                        future.cancel(true);  // Future 강제 취소
                        output.append("메모리 초과 발생: 메모리 사용량이 제한(")
                              .append(problem.getMemory())
                              .append("MB)을 초과했습니다.\n");
                        output.append("메모리 사용량을 줄이거나, 불필요한 객체 생성을 확인해 보세요.\n");
                        
                        results.add(ResultDto.builder()
                                .testNum(i+1)
                                .input(input)
                                .expectedResult(expectedOutput)
                                .actualResult(output.toString())
                                .status(ResultStatus.MEMORY_LIMIT)
                                .build());
                        break;
                    } else {
                        runProcess.destroy();
                        output.append("런타임 오류 발생: ").append(e.getCause().getMessage()).append("\n");
                        output.append("배열 인덱스 범위, null 참조, 형변환 오류 등을 확인해 보세요.\n");
                        
                        results.add(ResultDto.builder()
                                .testNum(i+1)
                                .input(input)
                                .expectedResult(expectedOutput)
                                .actualResult(output.toString())
                                .status(ResultStatus.RUNTIME_ERROR)
                                .build());
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
                        output.append("메모리 초과 발생: 메모리 사용량(")
                              .append(usedMemory / (1024 * 1024))
                              .append("MB)이 제한(")
                              .append(problem.getMemory())
                              .append("MB)을 초과했습니다.\n");
                        output.append("메모리 사용량을 줄이거나, 불필요한 객체 생성을 확인해 보세요.\n");
                        
                        results.add(ResultDto.builder()
                                .testNum(i+1)
                                .input(input)
                                .expectedResult(expectedOutput)
                                .actualResult(output.toString())
                                .status(ResultStatus.MEMORY_LIMIT)
                                .build());
                        break;
                    }
                }

                output.append("실행 결과:\n").append(result);

            } catch (Exception e) {
                output.append("예상치 못한 오류: ").append(e.getMessage()).append("\n");
            }

            // 통과 여부 확인
            boolean isPass = compareOutput(output.toString(), expectedOutput);
            if (isPass) {
                output.append("\n테스트 케이스 #").append(i + 1).append(" 통과!");
            } else {
                output.append("\n테스트 케이스 #").append(i + 1).append(" 실패");
                output.append("\n기대 출력: ").append(expectedOutput);
                output.append("\n실제 출력: ").append(output.toString());
                output.append("\n출력 형식과 타입을 확인해 보세요. 공백이나 줄바꿈에 주의하세요.");
            }

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
