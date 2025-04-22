package com.webproject.jandi_ide_backend.compiler.controller;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.compiler.service.CompilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 코드 컴파일 및 실행을 담당하는 컨트롤러
 * 
 * 이 컨트롤러는 사용자가 제출한 코드를 컴파일하고 실행하여 결과를 반환합니다.
 * 알고리즘 문제 풀이 및 코드 테스트 기능을 지원하며, Java, Python, C++ 언어를 지원합니다.
 */
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
     * 코드를 제출받아 컴파일 및 실행 후 결과를 반환합니다.
     * 문제 ID가 0인 경우에는 테스트 모드로 간주하여 컴파일과 실행 결과만 확인합니다.
     * 문제 ID가 0이 아닌 경우에는 해당 문제의 테스트 케이스로 코드를 검증합니다.
     * 
     * @param submissionDto 제출된 코드 정보 (사용자 ID, 문제 ID, 코드, 언어, 해결 시간)
     * @return 실행 결과 또는 오류 정보를 포함한 응답
     */
    @PostMapping("/submit")
    @Operation(
        summary = "코드 제출 및 실행",
        description = "사용자가 작성한 코드를 컴파일하고 실행하여 결과를 반환합니다. 문제 ID가 0인 경우는 테스트 모드로 동작합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "코드 컴파일 및 실행 성공", 
            content = @Content(schema = @Schema(implementation = Solution.class))
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
    public ResponseEntity<?> submitCode(
            @Parameter(description = "코드 제출 정보 (사용자 ID, 문제 ID, 코드, 언어, 해결 시간)", 
                      required = true) 
            @RequestBody CodeSubmissionDto submissionDto) {
        try {
            // 코드 컴파일 및 실행 요청
            Solution solution = compilerService.submitCode(submissionDto);
            return ResponseEntity.ok(solution);
        } catch (CompilerException e) {
            // 컴파일러 예외 처리 (컴파일 오류, 런타임 오류 등)
            CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Compilation Failed")
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .errorType(e.getErrorType().name())
                    .errorDetails(e.getErrorDetails())
                    .code(e.getCode())
                    .language(e.getLanguage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 일반 예외 처리 (서버 내부 오류)
            CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Server Error")
                    .message("서버 내부 오류가 발생했습니다: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 