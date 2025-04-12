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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // 기술 스택 , 선호 기업 , 대표 프로젝트는 각 DTO 생성후 추가 해야합니다.
}
