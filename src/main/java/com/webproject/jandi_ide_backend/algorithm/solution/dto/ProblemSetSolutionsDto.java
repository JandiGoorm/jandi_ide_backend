package com.webproject.jandi_ide_backend.algorithm.solution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문제집 및 풀이 정보 DTO")
public class ProblemSetSolutionsDto {

    @Schema(description = "문제집 ID")
    private Long problemSetId;
    
    @Schema(description = "문제집 이름")
    private String problemSetName;
    
    @Schema(description = "사용자 ID")
    private Long userId;
    
    @Schema(description = "문제 및 풀이 리스트")
    private List<ProblemSolutionDto> problems;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "문제 및 해당 문제 풀이 정보 DTO")
    public static class ProblemSolutionDto {
        
        @Schema(description = "문제 ID")
        private Integer problemId;
        
        @Schema(description = "문제 제목")
        private String problemTitle;
        
        @Schema(description = "문제 난이도")
        private Integer level;
        
        @Schema(description = "문제 설명")
        private String problemDescription;
        
        @Schema(description = "사용자의 풀이")
        private SolutionResponseDto solution;
    }
} 