package com.webproject.jandi_ide_backend.compiler.dto;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Solution 저장 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solution 저장 요청")
public class SaveSolutionDto {
    /**
     * 사용자 ID
     */
    @NotNull(message = "사용자 ID는 필수 항목이다")
    @Positive(message = "사용자 ID는 양수여야 한다")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    /**
     * 문제 ID
     */
    @NotNull(message = "문제 ID는 필수 항목이다")
    @Positive(message = "문제 ID는 양수여야 한다")
    @Schema(description = "문제 ID", example = "1")
    private Integer problemId;
    
    /**
     * 문제집 ID
     */
    @Schema(description = "문제집 ID", example = "1")
    private Long problemSetId;
    
    /**
     * 제출한 코드
     */
    @NotBlank(message = "코드는 필수 항목이다")
    @Size(max = 50000, message = "코드는 50,000자를 초과할 수 없다")
    @Schema(description = "제출한 코드")
    private String code;
    
    /**
     * 프로그래밍 언어
     */
    @NotBlank(message = "언어는 필수 항목이다")
    @Pattern(regexp = "^(java|python|c\\+\\+|JAVA|PYTHON|C\\+\\+)$", message = "지원 언어: java, python, c++")
    @Schema(description = "프로그래밍 언어", example = "java")
    private String language;
    
    /**
     * 정답 여부
     */
    @Schema(description = "정답 여부", example = "true")
    private Boolean isCorrect;
    
    /**
     * 문제 해결 시간 (초 단위)
     */
    @Schema(description = "문제 해결 시간 (초 단위)", example = "120")
    private Integer solvingTime;
    
    /**
     * 실행 결과 상세 정보
     */
    @Schema(description = "실행 결과 상세 정보")
    private String additionalInfo;
    
    /**
     * 메모리 사용량 (KB)
     */
    @Schema(description = "메모리 사용량 (KB)", example = "1024")
    private Integer memoryUsage;
    
    /**
     * 실행 시간 (ms)
     */
    @Schema(description = "실행 시간 (ms)", example = "10")
    private Integer executionTime;
    
    /**
     * 상태
     */
    @Schema(description = "상태", example = "CORRECT")
    private SolutionStatus status;
    
    /**
     * 설명
     */
    @Schema(description = "설명")
    private String description;
} 