package com.webproject.jandi_ide_backend.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BlobResponseDTO {
    @Schema (description = "해당 blob 의 해시값", example = "5sdb1sd5b3sdv5v1")
    private String sha;

    @Schema (description = "깃헙 내부에서 사용하는 고유 식별자", example = "1asdvsd65dv5sb1sdb1rtu5")
    private String node_id;

    @Schema (description = "해당 blob 의 url", example = "https://api.github.com/repos/Yoonhwi/jandi_plan_frontend/git/blobs/5sdb1sd5b3sdv5v1")
    private String url;

    @Schema (description = "해당 blob 의 크기", example = "640")
    private Long size;

    @Schema (description = "파일의 내용이 base64로 인코딩된 값 디코딩 시 실제 파일내용", example = "15656sdavasdvsv5115")
    private String content;

    @Schema (description = "파일의 인코딩 방식", example = "base64")
    private String encoding;
}
