package com.webproject.jandi_ide_backend.algorithm.testCase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponseDTO {

    @Schema(description = "테스트 케이스 ID", example = "1")
    private Integer id;

    @Schema(description = "입력으로 주어질 값", example = "1")
    private String input;

    @Schema(description = "기대 출력 값", example = "2")
    private String output;

    @Schema(description = "생성 시간", example = "2025-04-21T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "업데이트 시간", example = "2025-04-21T10:30:00")
    private LocalDateTime updatedAt;
}
