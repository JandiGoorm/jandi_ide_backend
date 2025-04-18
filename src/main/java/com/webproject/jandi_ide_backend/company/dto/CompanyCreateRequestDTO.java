package com.webproject.jandi_ide_backend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "회사 생성 요청")
public class CompanyCreateRequestDTO {
    @NotBlank(message = "회사 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "회사 이름은 1-30자 사이여야 합니다")
    @Schema(description = "회사 이름", example = "네이버")
    private String name;

    @Size(max = 500, message = "회사 설명은 500자 이하여야 합니다")
    @Schema(description = "회사 설명", example = "대한민국의 대표적인 인터넷 기업입니다.")
    private String description;
}