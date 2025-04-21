package com.webproject.jandi_ide_backend.compiler.controller;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.compiler.dto.CodeSubmissionDto;
import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.compiler.service.CompilerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/compiler")
public class CompilerController {

    private final CompilerService compilerService;
    
    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }
    
    /**
     * 코드를 제출받아 컴파일 및 실행 후 결과를 반환합니다.
     * @param submissionDto 제출된 코드 정보
     * @return 저장된 Solution 엔티티
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitCode(@RequestBody CodeSubmissionDto submissionDto) {
        try {
            Solution solution = compilerService.submitCode(submissionDto);
            return ResponseEntity.ok(solution);
        } catch (CompilerException e) {
            // 컴파일러 예외 처리
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
            // 일반 예외 처리
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