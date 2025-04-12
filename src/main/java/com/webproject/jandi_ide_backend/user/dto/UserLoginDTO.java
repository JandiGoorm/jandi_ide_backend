package com.webproject.jandi_ide_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserLoginDTO {
    @Schema(description = "Access Token", example = "asdbasdv123")
    private String accessToken;

    public UserLoginDTO( String accessToken) {
        this.accessToken = accessToken;
    }
}
