package com.webproject.jandi_ide_backend.compiler.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CompilerErrorResponseDto {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String errorType; // COMPILATION_ERROR, RUNTIME_ERROR 등
    private String errorDetails; // 자세한 컴파일러 오류 메시지
    private String code; // 제출한 코드
    private String language; // 제출한 언어
} 