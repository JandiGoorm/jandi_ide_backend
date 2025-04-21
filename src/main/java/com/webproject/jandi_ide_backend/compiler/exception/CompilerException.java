package com.webproject.jandi_ide_backend.compiler.exception;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import lombok.Getter;

@Getter
public class CompilerException extends RuntimeException {
    private final SolutionStatus errorType;
    private final String errorDetails;
    private final String code;
    private final String language;

    public CompilerException(String message, SolutionStatus errorType, String errorDetails, String code, String language) {
        super(message);
        this.errorType = errorType;
        this.errorDetails = errorDetails;
        this.code = code;
        this.language = language;
    }
} 