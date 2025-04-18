package com.webproject.jandi_ide_backend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CompanyUpdateRequestDTO {

    @Schema(description = "회사 이름", example = "네이버")
    private String name;

    @Schema(description = "회사 설명", example = "대한민국의 대표적인 인터넷 기업입니다.")
    private String description;

    @Schema(description = "회사 알고리즘 난이도", example = "[1, 2]")
    private List<Integer> tags;
}
