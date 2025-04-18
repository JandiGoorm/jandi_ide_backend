package com.webproject.jandi_ide_backend.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    private Integer id;
    private String githubId;
    private String profileImage;
    private String introduction;
    private String email;
    private String nickName;

    private String githubUsername;
    private User.UserRole Role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
