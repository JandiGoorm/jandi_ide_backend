package com.webproject.jandi_ide_backend.jobPosting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostingRequestDTO {

    @Schema(description = "공고명",example = "24년도 OO 하반기 공채")
    private String title;

    @Schema(description = "공고 설명")
    private String description;
}
