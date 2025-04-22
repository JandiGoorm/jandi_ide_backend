package com.webproject.jandi_ide_backend.algorithm.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProblemResponseDTO {

    @Schema(description = "문제 고유 ID", example = "1")
    private Integer id;

    @Schema(description = "문제 제목", example = "A+B")
    private String title;

    @Schema(description = "문제 설명", example = "주어진 정수 배열에서 가장 큰 값을 구하세요.")
    private String description;

    @Schema(description = "문제 난이도 (1~5)", example = "3")
    private Integer level;

    @Schema(description = "메모리 제한 (MB 단위)", example = "128")
    private Integer memory;

    @Schema(description = "시간 제한 (초 단위)", example = "1")
    private Integer timeLimit;

    @Schema(description = "문제 태그 목록", example = "[\"Array\", \"Sort\"]")
    private List<String> tags;

    @Schema(description = "문제 생성일", example = "2025-04-20T14:32:00")
    private LocalDateTime createdAt;

    @Schema(description = "문제 수정일", example = "2025-04-20T15:01:45")
    private LocalDateTime updatedAt;
}
