package com.webproject.jandi_ide_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RefreshDTO {

    @Schema(description = "refreshToken", example = "asdvasdvads56vv1")
    private String refreshToken;
}
