package com.webproject.jandi_ide_backend.algorithm.testCase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseRequestDTO {

    @Schema(description = "입력으로 주어질 값", example = "1")
    private String input;

    @Schema(description = "기대 출력 값", example = "2")
    private String output;
}
