package com.webproject.jandi_ide_backend.user.dto;
import lombok.Data;

@Data
public class UserInfoDTO {
    private String profileImage;
    private String email;
    private String githubId;
    private String nickname;

    public UserInfoDTO(String profileImage, String email, String githubId, String nickname) {
        this.profileImage = profileImage;
        this.email = email;
        this.githubId = githubId;
        this.nickname = nickname;
    }
}
