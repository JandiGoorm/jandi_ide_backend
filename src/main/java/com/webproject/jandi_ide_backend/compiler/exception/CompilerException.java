package com.webproject.jandi_ide_backend.compiler.exception;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import lombok.Getter;

/**
 * 컴파일러 관련 예외 클래스
 * 
 * 코드 컴파일이나 실행 과정에서 발생하는 다양한 오류 상황을 처리하기 위한 예외 클래스입니다.
 * 컴파일 오류, 런타임 오류, 타임아웃, 메모리 초과 등 여러 유형의 오류를 처리하며,
 * 각 오류에 대한 상세 정보를 포함하여 클라이언트에게 제공합니다.
 */
@Getter
public class CompilerException extends RuntimeException {
    /**
     * 오류 유형 (컴파일 에러, 런타임 에러, 시간 초과 등)
     */
    private final SolutionStatus errorType;
    
    /**
     * 상세 오류 메시지 (컴파일러 또는 런타임에서 반환한 구체적인 오류 정보)
     */
    private final String errorDetails;
    
    /**
     * 오류가 발생한 코드
     */
    private final String code;
    
    /**
     * 사용된 프로그래밍 언어
     */
    private final String language;

    /**
     * 컴파일러 예외 생성자
     * 
     * @param message 기본 오류 메시지
     * @param errorType 오류 유형 (SolutionStatus 열거형)
     * @param errorDetails 상세 오류 메시지
     * @param code 오류가 발생한 코드
     * @param language 사용 언어
     */
    public CompilerException(String message, SolutionStatus errorType, String errorDetails, String code, String language) {
        super(message);
        this.errorType = errorType;
        this.errorDetails = errorDetails;
        this.code = code;
        this.language = language;
    }
} 