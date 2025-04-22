package com.webproject.jandi_ide_backend.algorithm.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemRequestDTO {

    @Schema(title="문제의 제목", example = "A+B")
    private String title;

    @Schema(description = "문제에 대한 설명", example = "주어진 정수 배열에서 가장 큰 값을 구하세요.")
    private String description;

    @Min(1)
    @Max(5)
    @Schema(description = "문제 난이도 (1~5 사이 정수)", example = "3")
    private Integer level;

    @Schema(description = "메모리 제한 (MB 단위)", example = "256")
    private Integer memory;

    @Schema(description = "시간 제한 (초 단위)", example = "2")
    private Integer timeLimit;

    @Schema(description = "문제에 연결된 태그 목록", example = "[\"Array\", \"Sort\", \"DP\"]")
    private List<String> tags;
}
