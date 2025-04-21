package com.webproject.jandi_ide_backend.compiler.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultDto {
    private int testNum;
    private String input;
    private String expectedResult;
    private String actualResult;
    private Double executionTime;
    private Double usedMemory;
    private ResultStatus status;
} 