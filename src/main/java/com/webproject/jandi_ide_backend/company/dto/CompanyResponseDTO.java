package com.webproject.jandi_ide_backend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyResponseDTO {
    @Schema(description = "회사 ID", example = "1")
    private Integer id;

    @Schema(description = "회사 이름", example = "네이버")
    private String companyName;

    @Schema(description = "회사 설명", example = "대한민국의 대표적인 인터넷 기업입니다.")
    private String description;

    @Schema(description = "생성 일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2023-10-15T14:30:00")
    private LocalDateTime updatedAt;

    @Schema (description = "문제 난이도", example = "[1,1,2,3]")
    private List<Integer> levels = new ArrayList<>();

    @Schema (description = "코테 시간")
    private Integer timeInMinutes;

    @Schema (description = "코테에서 사용하는 언어", example = "[Python,Java]")
    private List<String> programmingLanguages = new ArrayList<>();
}
