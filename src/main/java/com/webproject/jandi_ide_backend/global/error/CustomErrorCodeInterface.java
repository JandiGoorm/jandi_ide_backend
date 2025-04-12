package com.webproject.jandi_ide_backend.global.error;

import org.springframework.http.HttpStatus;

/**
 * 모든 커스텀 에러 코드가 구현해야 하는 인터페이스
 * 에러 코드의 필수 요소들을 정의합니다.
 */
public interface CustomErrorCodeInterface {
    HttpStatus getStatusCode();

    String getErrorCode();

    String getMessage();

    String getTimestamp();
}
