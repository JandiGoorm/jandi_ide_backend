package com.webproject.jandi_ide_backend.compiler.dto;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 코드 컴파일 및 실행 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코드 컴파일 및 실행 결과")
public class CompileResultDto {
    /**
     * 컴파일 상태
     */
    @Schema(description = "컴파일 상태", example = "CORRECT")
    private SolutionStatus status;
    
    /**
     * 정답 여부
     */
    @Schema(description = "정답 여부", example = "true")
    private Boolean isCorrect;
    
    /**
     * 실행 결과 상세 정보
     */
    @Schema(description = "실행 결과 상세 정보")
    private String resultDetails;
    
    /**
     * 실행 시간 (ms)
     */
    @Schema(description = "실행 시간 (ms)", example = "10")
    private Integer executionTime;
    
    /**
     * 메모리 사용량 (KB)
     */
    @Schema(description = "메모리 사용량 (KB)", example = "1024")
    private Integer memoryUsage;
    
    /**
     * 테스트 케이스 결과 목록
     */
    @Schema(description = "테스트 케이스 결과 목록")
    private List<ResultDto> testResults;
    
    /**
     * 사용자 제출 코드
     */
    @Schema(description = "제출한 코드")
    private String code;
    
    /**
     * 프로그래밍 언어
     */
    @Schema(description = "프로그래밍 언어", example = "java")
    private String language;
} 