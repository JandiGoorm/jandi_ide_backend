package com.webproject.jandi_ide_backend.user.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AuthRequestDTO {

    @Schema(description = "GitHub OAuth code", example = "1234567bab")
    private String code;
    
    @Schema(description = "GitHub OAuth state parameter for CSRF protection", example = "random_state_string")
    private String state;
}
