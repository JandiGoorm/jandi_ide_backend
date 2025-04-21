package com.webproject.jandi_ide_backend.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코드 실행 결과 상태 열거형
 * 
 * 제출된 코드의 테스트 케이스 실행 결과 상태를 나타냅니다.
 */
@Schema(description = "코드 실행 결과 상태")
public enum ResultStatus {
    /**
     * 테스트 케이스 통과
     */
    PASS,
    
    /**
     * 테스트 케이스 실패 (결과가 기대 출력과 다름)
     */
    FAIL,
    
    /**
     * 테스트 케이스 실행 중 오류 발생
     */
    ERROR
} 