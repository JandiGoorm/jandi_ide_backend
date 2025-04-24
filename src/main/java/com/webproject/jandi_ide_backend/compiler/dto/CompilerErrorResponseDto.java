package com.webproject.jandi_ide_backend.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 컴파일러 오류 응답 DTO
 * 
 * 코드 컴파일 또는 실행 중 발생한 오류에 대한 상세 정보를 클라이언트에 제공하기 위한 객체입니다.
 * 컴파일 에러, 런타임 에러, 타임아웃, 메모리 초과 등 다양한 유형의 오류 정보를 포함합니다.
 */
@Getter
@Builder
@Schema(description = "컴파일러 오류 응답 정보")
public class CompilerErrorResponseDto {
    @Schema(description = "오류 유형", example = "Compilation Failed")
    private String error;
    
    @Schema(description = "오류 메시지", example = "자바 컴파일 에러가 발생했습니다")
    private String message;
    
    @Schema(description = "오류 발생 시간")
    private LocalDateTime timestamp;
    
    @Schema(description = "오류 타입 (COMPILATION_ERROR, RUNTIME_ERROR 등)", example = "COMPILATION_ERROR")
    private String errorType;
    
    @Schema(description = "상세 오류 메시지", example = "Main.java:10: error: ';' expected\n    System.out.println(\"Hello World\")\n                                    ^")
    private String errorDetails;
    
    @Schema(description = "제출한 코드")
    private String code;
    
    @Schema(description = "사용 언어", example = "java")
    private String language;
} 