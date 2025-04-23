package com.webproject.jandi_ide_backend.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectResponseDTO {
    @Schema(description = "프로젝트 ID", example = "1")
    private Integer id;

    @Schema(description = "프로젝트 이름", example = "algorithm")
    private String name;

    @Schema(description = "GitHub 레포지토리 이름", example = "algorithm")
    private String githubName;

    @Schema(description = "프로젝트 설명", example = "practice algorithm")
    private String description;

    @Schema(description = "프로젝트 URL", example = "https://github.com/username/algorithm")
    private String url;

    @Schema(description = "생성 일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2023-10-15T14:30:00")
    private LocalDateTime updatedAt;
}
