package com.webproject.jandi_ide_backend.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 코드 실행 결과 상태 열거형
 * 
 * 제출된 코드의 테스트 케이스 실행 결과 상태를 나타냅니다.
 * Solution 엔티티의 SolutionStatus와 일관성을 유지하도록 설계되었습니다.
 */
@Schema(description = "코드 실행 결과 상태")
public enum ResultStatus {
    /**
     * 제출됨 (테스트 케이스 실행 전)
     */
    SUBMITTED,
    
    /**
     * 테스트 케이스 평가 중
     */
    EVALUATING,
    
    /**
     * 테스트 케이스 통과
     */
    CORRECT,
    
    /**
     * 테스트 케이스 실패 (결과가 기대 출력과 다름)
     */
    WRONG_ANSWER,
    
    /**
     * 런타임 에러 발생
     */
    RUNTIME_ERROR,
    
    /**
     * 컴파일 에러 발생
     */
    COMPILATION_ERROR,
    
    /**
     * 시간 초과 발생
     */
    TIMEOUT,
    
    /**
     * 메모리 사용 제한 초과
     */
    MEMORY_LIMIT
} 