package com.webproject.jandi_ide_backend.compiler.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CodeSubmissionDto {
    private Long userId;
    private Integer problemId;
    private String code;
    private String language;
    private Integer solvingTime; // Optional
} 