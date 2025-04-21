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
    INVALID_GITHUB_CODE(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_CODE", "Invalid GitHub code"), // 유효하지 않은 깃헙 코드
    INVALID_GITHUB_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_GITHUB_TOKEN", "Invalid GitHub token"), // 유효하지 않은 깃헙 토큰
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_JWT_TOKEN", "Expired JWT token"), // 만료된 JWT 토큰
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_JWT_TOKEN", "Invalid JWT token"), // 유효하지 JWT 토큰
    JWT_TOKEN_CREATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_TOKEN_CREATION_ERROR", "JWT token creation failed"), // JWT 토큰 생성 실패
    GITHUB_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "GITHUB_LOGIN_FAILED", "GitHub login failed"), // 깃헙 로그인 실패
    GITHUB_API_FAILED(HttpStatus.BAD_REQUEST, "GITHUB_API_FAILED", "GitHub API failed"), // 깃헙 API 실패
    GITHUB_AUTH_EXPIRED(HttpStatus.UNAUTHORIZED, "GITHUB_AUTH_EXPIRED", "GitHub authentication expired or invalid"), // 깃헙 인증 만료 또는 유효하지 않음

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"), // 유저를 찾을 수 없음
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "Project not found"), // 프로젝트를 찾을 수 없음
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_NOT_FOUND", "Company not found"), // 회사를 찾을 수 없음
    JOBPOSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "JOBPOSTING_NOT_FOUND", "Jobposting not found"),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM_NOT_FOUND", "Problem not found"),
    TESTCASE_NOT_FOUND(HttpStatus.NOT_FOUND, "TESTCASE_NOT_FOUND", "Testcase not found"),

    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "PERMISSION_DENIED", "Permission denied"), // 권한 없음
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Invalid page"), // 유효하지 않은 페이지



    // 500번대 에러
    DB_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DB_OPERATION_FAILED", "Database operation failed"); // DB 작업 실패

    

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
