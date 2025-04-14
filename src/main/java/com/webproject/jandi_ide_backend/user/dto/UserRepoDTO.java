package com.webproject.jandi_ide_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRepoDTO {
    @Schema(description = "레포 소유자", example = "Yoonhwi")
    private String owner;

    @Schema(description = "레포 이름", example = "algorithm")
    private String name;

    @Schema(description = "레포 설명", example = "it's algorithm study repo")
    private String description;
    
    @Schema(description = "레포 링크 주소", example = "https:github.com/~~~/algorithm")
    private String htmlUrl;
    
    @Schema(description = "레포 마지막 업데이트", example = "2023-10-01T12:00:00")
    private LocalDateTime githubUpdatedAt;
}
