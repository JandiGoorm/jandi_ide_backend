package com.webproject.jandi_ide_backend.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 코드 제출 DTO
 * 
 * 사용자가 제출한 코드와 관련 정보를 담는 데이터 전송 객체입니다.
 * 문제 풀이 또는 코드 테스트를 위해 필요한 정보를 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코드 제출 정보")
public class CodeSubmissionDto {
    
    /**
     * 사용자 ID
     */
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    /**
     * 문제 ID (0인 경우 테스트 모드로 동작)
     */
    @Schema(description = "문제 ID (0인 경우 테스트 모드로 동작)", example = "0")
    private Long problemId;
    
    /**
     * 제출한 코드
     */
    @Schema(description = "제출한 코드", example = "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World\");\n    }\n}")
    private String code;
    
    /**
     * 프로그래밍 언어
     */
    @Schema(description = "프로그래밍 언어", example = "java")
    private String language;
    
    /**
     * 문제 해결 시간 (초 단위)
     */
    @Schema(description = "문제 해결 시간 (초 단위)", example = "120")
    private Integer solvingTime;
} 