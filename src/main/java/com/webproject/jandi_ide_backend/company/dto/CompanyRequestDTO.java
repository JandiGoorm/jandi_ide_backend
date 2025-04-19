package com.webproject.jandi_ide_backend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequestDTO {

    @NotBlank(message = "회사 이름은 필수입니다")
    @Size(min = 1, max = 100, message = "회사 이름은 1-30자 사이여야 합니다")
    @Schema(description = "회사 이름", example = "네이버")
    private String companyName;

    @Size(max = 500, message = "회사 설명은 500자 이하여야 합니다")
    @Schema(description = "회사 설명", example = "대한민국의 대표적인 인터넷 기업입니다.")
    private String description;

    @Schema(description = "코딩 테스트 문제 난이도 목록", example = "[1, 2, 3]")
    private List<Integer> levels;

    @Schema(description = "풀이 시간 (분)", example = "90")
    private Integer timeInMinutes;

    @Schema(description = "사용 프로그래밍 언어 목록", example = "[\"Java\", \"Python\"]")
    private List<String> programmingLanguages;
}
