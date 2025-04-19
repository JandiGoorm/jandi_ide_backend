package com.webproject.jandi_ide_backend.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 생성 요청")
public class ProjectCreateRequestDTO {
    @NotBlank(message = "프로젝트 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "프로젝트 이름은 1-100자 사이여야 합니다")
    @Schema(description = "프로젝트 이름", example = "algorithm")
    private String name;

    @NotBlank(message = "GitHub 레포지토리 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "GitHub 레포지토리 이름은 1-100자 사이여야 합니다")
    @Schema(description = "GitHub 레포지토리 이름", example = "algorithm")
    private String githubName;

    @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다")
    @Schema(description = "프로젝트 설명", example = "practice algorithm")
    private String description;

    @NotBlank(message = "프로젝트 URL은 필수입니다")
    @Size(max = 255, message = "URL은 255자 이하여야 합니다")
    @Schema(description = "GitHub 프로젝트 URL", example = "https://github.com/username/algorithm")
    private String url;
}