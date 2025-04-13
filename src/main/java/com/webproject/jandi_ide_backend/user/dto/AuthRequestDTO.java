package com.webproject.jandi_ide_backend.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthRequestDTO {

    @Schema(description = "GitHub OAuth code", example = "1234567bab")
    private String code;
}
