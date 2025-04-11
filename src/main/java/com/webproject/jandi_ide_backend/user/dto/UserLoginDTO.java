package com.webproject.jandi_ide_backend.user.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String accessToken;

    public UserLoginDTO( String accessToken) {
        this.accessToken = accessToken;
    }
}
