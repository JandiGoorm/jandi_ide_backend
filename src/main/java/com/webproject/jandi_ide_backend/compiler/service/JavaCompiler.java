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
            javaProcess.redirectErrorStream(true); // 표준 에러를 표준 출력으로 리다이렉트
            try {
                Process runProcess = javaProcess.start();

                // 테스트 케이스 입력 전달
                OutputStream stdin = runProcess.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
                
                // 콤마로 구분된 입력을 처리하도록 수정
                String[] testcaseInputs = input.split(",");
                for (String inputLine : testcaseInputs) {
                    writer.write(inputLine.trim());
                    writer.newLine();
                }
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
                        output.append("알고리즘의 복잡도를 개선하거나, 무한 루프를 확인해 보세요.\n");
                        
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
                    long endTime = System.nanoTime();
                    long endMemory = getUsedMemory();
                    
                    // 자원 정리
                    reader.close();
                    stdout.close();
                    runProcess.destroyForcibly();
                    
                    Double executionTime = (double) TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                    Double usedMemoryMB = (double) ((endMemory - startMemory) / (1024 * 1024));
                    
                    time = executionTime;
                    memory = usedMemoryMB;
                }

                // 결과 저장
                String resultString = result.toString().trim();
                log.debug("Raw output capture: [" + resultString + "]");
                output.append("실행 결과: ").append(resultString);
                
                // 통과 여부 확인
                boolean isPass = compareOutput(resultString, expectedOutput);
                if (isPass) {
                    output.append("\n테스트 케이스 #").append(i + 1).append(" 통과!");
                } else {
                    output.append("\n테스트 케이스 #").append(i + 1).append(" 실패");
                    output.append("\n기대 출력: ").append(expectedOutput);
                    output.append("\n실제 출력: ");
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
            } catch (Exception e) {
                output.append("예상치 못한 오류: ").append(e.getMessage()).append("\n");
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
        
        // 공백 제거 후 직접 비교
        String actualOutput = actual.trim().replaceAll("\\s+", "");
        String expectedOutput = expected.trim().replaceAll("\\s+", "");
        
        log.debug("After cleanup - Actual: [" + actualOutput + "], Expected: [" + expectedOutput + "]");
        
        return actualOutput.equals(expectedOutput);
    }
}
