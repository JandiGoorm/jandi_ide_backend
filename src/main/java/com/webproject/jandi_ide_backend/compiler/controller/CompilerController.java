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
import java.time.LocalDateTime;

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
            description = "코드 컴파일 및 실행 성공 또는 컴파일/실행 중 오류 발생", 
            content = @Content(schema = @Schema(oneOf = {CompileResultDto.class, CompilerErrorResponseDto.class}))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        )
    })
    public ResponseEntity<?> compileCode(
            @Parameter(description = "코드 제출 정보 (사용자 ID, 문제 ID, 코드, 언어, 해결 시간)", 
                      required = true) 
            @RequestBody CodeSubmissionDto submissionDto) {
        // 사용자 제출 정보 로깅
        log.debug("코드 컴파일: 사용자={}, 문제={}, 언어={}", 
            submissionDto.getUserId(), 
            submissionDto.getProblemId(), 
            submissionDto.getLanguage());
            
        try {
            // 기본적인 null 체크와 빈 문자열 체크만 수행
            if (submissionDto.getCode() == null || submissionDto.getCode().trim().isEmpty()) {
                return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                    .status(400)
                    .error("Invalid Input")
                    .message("코드는 비어있을 수 없습니다")
                    .timestamp(LocalDateTime.now())
                    .errorType("COMPILATION_ERROR")
                    .errorDetails("코드가 비어있습니다. 코드를 입력해주세요.")
                    .code("")
                    .language(submissionDto.getLanguage())
                    .build());
            }
            
            // 코드 컴파일 및 실행 (저장하지 않음)
            CompileResultDto result = compilerService.compileCode(submissionDto);
            return ResponseEntity.ok(result);
        } catch (CompilerException e) {
            // 컴파일러 예외를 응답 본문에 포함하여 HTTP 200으로 반환
            return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                .status(400)
                .error(e.getErrorType() != null ? e.getErrorType().name() : "Compilation Failed")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errorType(e.getErrorType() != null ? e.getErrorType().name() : "COMPILATION_ERROR")
                .errorDetails(e.getErrorDetails())
                .code(e.getCode())
                .language(e.getLanguage())
                .build());
        } catch (Exception e) {
            // 기타 예외는 서버 오류로 처리하지만 HTTP 200 반환
            log.error("Unexpected error during compilation:", e);
            return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                .status(500)
                .error("Internal Server Error")
                .message("코드 컴파일 중 예상치 못한 오류가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .errorType("SERVER_ERROR")
                .errorDetails(e.getMessage())
                .code(submissionDto.getCode())
                .language(submissionDto.getLanguage())
                .build());
        }
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
            description = "솔루션 저장 성공 또는 저장 중 오류 발생", 
            content = @Content(schema = @Schema(oneOf = {Solution.class, CompilerErrorResponseDto.class}))
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류", 
            content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
        )
    })
    public ResponseEntity<?> saveSolution(
            @Parameter(description = "Solution 저장 요청 정보", required = true) 
            @RequestBody SaveSolutionDto saveSolutionDto) {
        // 사용자 제출 정보 로깅
        log.debug("솔루션 저장: 사용자={}, 문제={}, 언어={}", 
            saveSolutionDto.getUserId(), 
            saveSolutionDto.getProblemId(), 
            saveSolutionDto.getLanguage());
        
        try {
            // 기본적인 null 체크와 빈 문자열 체크만 수행
            if (saveSolutionDto.getCode() == null || saveSolutionDto.getCode().trim().isEmpty()) {
                return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                    .status(400)
                    .error("Invalid Input")
                    .message("코드는 비어있을 수 없습니다")
                    .timestamp(LocalDateTime.now())
                    .errorType("COMPILATION_ERROR")
                    .errorDetails("코드가 비어있습니다. 코드를 입력해주세요.")
                    .code("")
                    .language(saveSolutionDto.getLanguage())
                    .build());
            }
                
            // Solution 저장
            Solution solution = compilerService.saveSolution(saveSolutionDto);
            return ResponseEntity.ok(solution);
        } catch (CompilerException e) {
            // 컴파일러 예외를 응답 본문에 포함하여 HTTP 200으로 반환
            return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                .status(400)
                .error(e.getErrorType() != null ? e.getErrorType().name() : "Save Failed")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errorType(e.getErrorType() != null ? e.getErrorType().name() : "SAVE_ERROR")
                .errorDetails(e.getErrorDetails())
                .code(e.getCode())
                .language(e.getLanguage())
                .build());
        } catch (Exception e) {
            // 기타 예외는 서버 오류로 처리하지만 HTTP 200 반환
            log.error("Unexpected error during solution save:", e);
            return ResponseEntity.ok(CompilerErrorResponseDto.builder()
                .status(500)
                .error("Internal Server Error")
                .message("솔루션 저장 중 예상치 못한 오류가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .errorType("SERVER_ERROR")
                .errorDetails(e.getMessage())
                .code(saveSolutionDto.getCode())
                .language(saveSolutionDto.getLanguage())
                .build());
        }
    }
} 