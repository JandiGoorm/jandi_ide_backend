package com.webproject.jandi_ide_backend.user.dto;

import lombok.Data;

@Data
public class UserRespDTO {
    private Integer id;
    private String githubId;
    private String profileImage;
    private String introduction;

    // 기술 스택 , 선호 기업 , 대표 프로젝트는 각 DTO 생성후 추가 해야합니다.
}
