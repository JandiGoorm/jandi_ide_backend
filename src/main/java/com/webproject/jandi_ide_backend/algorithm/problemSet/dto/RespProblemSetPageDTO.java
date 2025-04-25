package com.webproject.jandi_ide_backend.algorithm.problemSet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RespProblemSetPageDTO {
    @Schema(description = "페이지에 해당하는 문제집 리스트")
    private List<RespProblemSetDTO> data;

    @Schema(description = "현재 페이지",example = "0")
    private int currentPage;

    @Schema(description = "페이지당 항목 수", example = "10")
    private int size;

    @Schema(description = "전체 아이템 수",example = "50")
    private long totalItems;

    @Schema(description = "총 페이지 수",example = "5")
    private int totalPages;
}
