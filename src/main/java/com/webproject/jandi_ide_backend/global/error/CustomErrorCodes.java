package com.webproject.jandi_ide_backend.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;


/**
 * 에러 코드 집합 (열거형)
 * 모든 커스텀 에러 코드를 Enum으로 관리합니다.
 */
@Getter
public enum CustomErrorCodes implements CustomErrorCodeInterface {
    // 400번대 에러
    INVALID_GITHUB_CODE(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_CODE", "Invalid GitHub code"),
    INVALID_GITHUB_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_TOKEN", "Invalid GitHub token"),
    GITHUB_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "GITHUB_LOGIN_FAILED", "GitHub login failed"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");

    private final HttpStatus statusCode;
    private final String errorCode;
    private final String message;
    private final String timestamp;

    CustomErrorCodes(HttpStatus statusCode, String errorCode, String message) {
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }
}
