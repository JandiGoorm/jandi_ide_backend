package com.webproject.jandi_ide_backend.user.dto;

import com.webproject.jandi_ide_backend.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<String> techStacks;
    private List<String> favoriteCompanies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
