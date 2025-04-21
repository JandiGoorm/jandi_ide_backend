package com.webproject.jandi_ide_backend.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 코드 실행 결과 DTO
 * 
 * 제출된 코드의 테스트 케이스별 실행 결과를 담는 데이터 전송 객체입니다.
 * 테스트 번호, 입력 값, 기대 결과, 실제 결과, 실행 시간, 메모리 사용량, 상태 정보를 포함합니다.
 */
@Getter
@Builder
@Schema(description = "코드 실행 결과 정보")
public class ResultDto {
    /**
     * 테스트 케이스 번호
     */
    @Schema(description = "테스트 케이스 번호", example = "1")
    private int testNum;
    
    /**
     * 테스트 케이스 입력 값
     */
    @Schema(description = "테스트 케이스 입력 값", example = "10 20")
    private String input;
    
    /**
     * 기대 출력 결과
     */
    @Schema(description = "기대 출력 결과", example = "30")
    private String expectedResult;
    
    /**
     * 실제 출력 결과
     */
    @Schema(description = "실제 출력 결과", example = "30")
    private String actualResult;
    
    /**
     * 실행 소요 시간 (밀리초 단위)
     */
    @Schema(description = "실행 소요 시간 (밀리초 단위)", example = "12.5")
    private Double executionTime;
    
    /**
     * 사용 메모리 (MB 단위)
     */
    @Schema(description = "사용 메모리 (MB 단위)", example = "32.4")
    private Double usedMemory;
    
    /**
     * 테스트 결과 상태 (통과, 실패, 오류)
     */
    @Schema(description = "테스트 결과 상태", example = "PASS")
    private ResultStatus status;
} 