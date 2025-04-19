package com.webproject.jandi_ide_backend.jobPosting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostingResponseDTO {
    @Schema(description = "채용 공고 id값",example = "1")
    private Integer id;

    @Schema(description = "채용 공고 이름",example = "25년도 하반기 OO 공채")
    private String title;

    @Schema(description = "채용 공고 설명",example = "")
    private String description;

    @Schema(description = "채용 공고의 일정 리스트")
    private List<ScheduleResponseDTO> schedules;

    @Schema(description = "생성 일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2023-10-15T14:30:00")
    private LocalDateTime updatedAt;
}
