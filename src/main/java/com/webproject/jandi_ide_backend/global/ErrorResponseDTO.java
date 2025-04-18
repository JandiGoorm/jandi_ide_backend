package com.webproject.jandi_ide_backend.global;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    @Schema(description = "HTTP 상태 코드", example = "400")
    private int status;

    @Schema(description = "에러 요약", example = "Bad Request")
    private String error;

    @Schema(description = "상세 에러 메시지", example = "요청한 파라미터가 잘못되었습니다.")
    private String message;

    @Schema(description = "에러 발생 시간", example = "2025-04-12T14:32:00")
    private String timestamp;
}
