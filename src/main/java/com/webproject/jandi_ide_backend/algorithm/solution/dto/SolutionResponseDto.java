package com.webproject.jandi_ide_backend.algorithm.solution.dto;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution;
import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "풀이 응답 DTO")
public class SolutionResponseDto {

    @Schema(description = "풀이 ID")
    private Long id;

    @Schema(description = "사용자 ID")
    private Integer userId;

    @Schema(description = "사용자 이름")
    private String userName;

    @Schema(description = "문제 ID")
    private Integer problemId;

    @Schema(description = "문제집 ID")
    private Long problemSetId;

    @Schema(description = "코드")
    private String code;

    @Schema(description = "프로그래밍 언어")
    private String language;

    @Schema(description = "풀이 시간 (초)")
    private Integer solvingTime;

    @Schema(description = "정답 여부")
    private Boolean isCorrect;

    @Schema(description = "상태")
    private SolutionStatus status;

    @Schema(description = "추가 정보")
    private String additionalInfo;

    @Schema(description = "메모리 사용량 (MB)")
    private Integer memoryUsage;

    @Schema(description = "실행 시간 (ms)")
    private Integer executionTime;

    @Schema(description = "코드 설명")
    private String description;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;
    
    /**
     * Solution 엔티티를 SolutionResponseDto로 변환
     */
    public static SolutionResponseDto fromEntity(Solution solution) {
        return SolutionResponseDto.builder()
                .id(solution.getId())
                .userId(solution.getUser() != null ? solution.getUser().getId() : null)
                .userName(solution.getUser() != null ? solution.getUser().getNickname() : null)
                .problemId(solution.getProblemId())
                .problemSetId(solution.getProblemSetId())
                .code(solution.getCode())
                .language(solution.getLanguage())
                .solvingTime(solution.getSolvingTime())
                .isCorrect(solution.getIsCorrect())
                .status(solution.getStatus())
                .additionalInfo(solution.getAdditionalInfo())
                .memoryUsage(solution.getMemoryUsage())
                .executionTime(solution.getExecutionTime())
                .description(solution.getDescription())
                .createdAt(solution.getCreatedAt())
                .updatedAt(solution.getUpdatedAt())
                .build();
    }
} 