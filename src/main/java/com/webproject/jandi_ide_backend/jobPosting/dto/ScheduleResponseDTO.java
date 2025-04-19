package com.webproject.jandi_ide_backend.jobPosting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleResponseDTO {

    @Schema(description = "스케쥴의 id값",example = "1")
    private Integer id;

    @Schema(description = "스케쥴의 이름", example = "1차 면접")
    private String scheduleName;

    @Schema(description = "스케쥴의 날짜", example = "2025-04-19")
    private LocalDate date;

    @Schema(description = "스케쥴의 설명", example = "오전 10시부터 오후 12시까지 진행되는 기술면접 입니다.")
    private String description;

    @Schema(description = "생성 일시", example = "2025-04-19T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-04-19T14:30:00")
    private LocalDateTime updatedAt;
}
