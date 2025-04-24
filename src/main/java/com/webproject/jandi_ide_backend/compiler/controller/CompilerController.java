package com.webproject.jandi_ide_backend.compiler.controller;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompileResultDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.dto.SaveSolutionDto;
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.compiler.service.CompilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import java.util.Stack;
import java.util.Map;

/**
 * 코드 컴파일 및 실행을 담당하는 컨트롤러
 * 
 * 이 컨트롤러는 사용자가 제출한 코드를 컴파일하고 실행하여 결과를 반환합니다.
 * 알고리즘 문제 풀이 및 코드 테스트 기능을 지원하며, Java, Python, C++ 언어를 지원합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/compiler")
@Tag(name = "컴파일러 API", description = "코드 컴파일 및 실행을 위한 API")
public class CompilerController {

    private final CompilerService compilerService;
    
    /**
     * 컴파일러 컨트롤러 생성자
     * 
     * @param compilerService 코드 컴파일 및 실행 서비스
     */
    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }
    
    /**
     * 코드를 컴파일하고 실행하여 결과만 반환합니다. 솔루션을 저장하지 않습니다.
     * 
     * @param submissionDto 제출된 코드 정보
     * @return 실행 결과를 포함한 응답
     */
    @PostMapping("/compile")
    @Operation(
        summary = "코드 컴파일 및 실행",
        description = "사용자가 작성한 코드를 컴파일하고 실행하여 결과만 반환합니다. 솔루션을 저장하지 않습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "코드 컴파일 및 실행 성공", 
            content = @Content(schema = @Schema(implementation = CompileResultDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "컴파일 또는 실행 중 오류 발생", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        )
    })
    public ResponseEntity<CompileResultDto> compileCode(
            @Parameter(description = "코드 제출 정보 (사용자 ID, 문제 ID, 코드, 언어, 해결 시간)", 
                      required = true) 
            @RequestBody CodeSubmissionDto submissionDto) {
        // 사용자 제출 정보 로깅
        log.debug("코드 컴파일: 사용자={}, 문제={}, 언어={}", 
            submissionDto.getUserId(), 
            submissionDto.getProblemId(), 
            submissionDto.getLanguage());
            
        // 코드 유효성 검사
        validateCode(submissionDto.getCode(), submissionDto.getLanguage());
        
        // 코드 컴파일 및 실행 (저장하지 않음)
        CompileResultDto result = compilerService.compileCode(submissionDto);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 코드 실행 결과를 Solution 테이블에 저장합니다.
     * 
     * @param saveSolutionDto Solution 저장 요청 정보
     * @return 저장된 Solution 객체
     */
    @PostMapping("/save-solution")
    @Operation(
        summary = "솔루션 저장",
        description = "코드 실행 결과를 Solution 테이블에 저장합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "솔루션 저장 성공", 
            content = @Content(schema = @Schema(implementation = Solution.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        )
    })
    public ResponseEntity<Solution> saveSolution(
            @Parameter(description = "Solution 저장 요청 정보", required = true) 
            @RequestBody SaveSolutionDto saveSolutionDto) {
        // 사용자 제출 정보 로깅
        log.debug("솔루션 저장: 사용자={}, 문제={}, 언어={}", 
            saveSolutionDto.getUserId(), 
            saveSolutionDto.getProblemId(), 
            saveSolutionDto.getLanguage());
            
        // Solution 저장
        Solution solution = compilerService.saveSolution(saveSolutionDto);
        return ResponseEntity.ok(solution);
    }
    
    /**
     * 코드의 기본적인 유효성을 검사합니다.
     * 간단한 구문 오류(세미콜론 누락, 괄호 불일치 등)를 사전에 감지합니다.
     */
    private void validateCode(String code, String language) {
        if (code == null || code.trim().isEmpty()) {
            throw new CompilerException("code cannot be null or empty", Solution.SolutionStatus.COMPILATION_ERROR);
        }

        // Java 코드 검증
        if ("java".equalsIgnoreCase(language)) {
            // 세미콜론 체크 - 더 정확한 세미콜론 체크 로직
            String[] lines = code.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                // 주석, 빈 줄, 클래스/메서드 선언부, 괄호만 있는 줄, if/else/for/while 조건부, import 문은 제외
                if (line.isEmpty() || 
                    line.startsWith("//") || 
                    line.startsWith("/*") || 
                    line.startsWith("*") || 
                    line.startsWith("*/") ||
                    line.startsWith("package ") ||
                    line.startsWith("import ") ||
                    line.matches(".*\\{\\s*") ||  // 열린 중괄호로 끝나는 줄
                    line.matches("\\s*\\}.*") ||  // 닫힌 중괄호로 시작하는 줄
                    line.matches(".*\\s+class\\s+.*") ||
                    (line.contains("if") && line.contains("(") && !line.contains("{")) ||
                    (line.contains("for") && line.contains("(") && !line.contains("{")) ||
                    (line.contains("while") && line.contains("(") && !line.contains("{"))) {
                    continue;
                }
                
                // 세미콜론으로 끝나지 않는 구문 검사
                // 변수 선언, 값 할당, 메서드 호출 등은 세미콜론으로 끝나야 함
                if (!line.endsWith(";") && 
                    !line.endsWith("}") && 
                    !line.endsWith("{") && 
                    !line.contains("//") &&  // 주석 포함 라인은 건너뜀
                    line.matches(".*\\b(int|double|float|char|boolean|String|long|byte|short)\\b.*=.*") ||  // 변수 선언/할당
                    line.matches(".*\\.[a-zA-Z0-9_]+\\(.*\\).*") ||  // 메서드 호출
                    line.matches(".*=\\s*[a-zA-Z0-9_]+.*") ||  // 값 할당
                    line.matches(".*\\bInteger\\.parseInt\\(.*\\).*") ||  // Integer.parseInt 호출
                    line.matches(".*\\bDouble\\.parseDouble\\(.*\\).*") ||  // Double.parseDouble 호출
                    line.matches(".*\\bSystem\\.out\\..*") ||  // System.out.println/print 호출
                    line.matches(".*[a-zA-Z0-9_]+\\(.*\\).*")) {  // 일반 메서드 호출
                    
                    throw new CompilerException(
                        "Syntax error: missing semicolon at line " + (i + 1) + ": \"" + line + "\"", 
                        Solution.SolutionStatus.COMPILATION_ERROR);
                }
            }

            // 괄호 균형 체크
            checkBracketBalance(code);

            // Java 클래스 구조 체크 (클래스 선언이 있는지 확인)
            if (!code.contains("class ")) {
                throw new CompilerException("Java code must contain a class declaration", Solution.SolutionStatus.COMPILATION_ERROR);
            }
        }
        // 추가 언어에 대한 검증 (Python, C++ 등)은 필요에 따라 구현
    }

    /**
     * 코드에서 괄호의 균형이 맞는지 확인합니다.
     */
    private void checkBracketBalance(String code) {
        Stack<Character> stack = new Stack<>();
        Map<Character, Character> bracketPairs = Map.of(
            ')', '(',
            '}', '{',
            ']', '['
        );
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            // 여는 괄호는 스택에 추가
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            }
            // 닫는 괄호는 스택에서 매칭되는 여는 괄호를 확인
            else if (c == ')' || c == '}' || c == ']') {
                if (stack.isEmpty() || stack.pop() != bracketPairs.get(c)) {
                    throw new CompilerException(
                        "Syntax error: unbalanced brackets. Check if all opened brackets are properly closed.", 
                        Solution.SolutionStatus.COMPILATION_ERROR);
                }
            }
        }
        
        // 스택이 비어있지 않다면 여는 괄호가 닫히지 않은 것
        if (!stack.isEmpty()) {
            throw new CompilerException(
                "Syntax error: unclosed brackets. Make sure all brackets are properly closed.", 
                Solution.SolutionStatus.COMPILATION_ERROR);
        }
    }
} 