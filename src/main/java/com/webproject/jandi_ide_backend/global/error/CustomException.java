package com.webproject.jandi_ide_backend.global.error;

import lombok.Getter;

/**
 * 커스텀 예외 클래스
 * RuntimeException을 상속받아 예외 처리를 커스텀화합니다.
 */
@Getter
public class CustomException extends RuntimeException{
    private final CustomErrorCodeInterface customErrorCode;

    public CustomException(CustomErrorCodeInterface errorCode) {
        this.customErrorCode = errorCode;
    }
}
