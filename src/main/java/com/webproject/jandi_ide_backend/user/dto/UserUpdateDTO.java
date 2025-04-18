package com.webproject.jandi_ide_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateDTO {
    private String email;
    private String introduction;
    private String profileImage;
    private String nickname;
}
