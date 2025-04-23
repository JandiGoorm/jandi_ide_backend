package com.webproject.jandi_ide_backend.global;

import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.dto.ResultStatus;
import com.webproject.jandi_ide_backend.compiler.exception.CompilerException;
import com.webproject.jandi_ide_backend.global.error.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 클래스.
 * 애플리케이션 전반에서 발생하는 RuntimeException(및 그 하위 예외)을 잡아서
 * 클라이언트에게 표준화된 에러 응답을 보내는 역할을 함.
 */
@RestControllerAdvice
@Slf4j
@Order(100) // Lower precedence than CompilerExceptionHandler
public class GlobalExceptionHandler {

    @ExceptionHandler(CompilerException.class)
    public ResponseEntity<CompilerErrorResponseDto> handleCompilerException(CompilerException ex) {
        log.error("Compiler exception occurred: {}", ex.getMessage());
        log.error("Error details: {}", ex.getErrorDetails());
        
        CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ex.getErrorType().name())
                .errorType(ex.getErrorType().name())
                .message(ex.getMessage())
                .errorDetails(ex.getErrorDetails())
                .language(ex.getLanguage())
                .timestamp(LocalDateTime.now())
                .code(ex.getCode())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * NullPointerException 처리
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponseDTO> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.error("NullPointerException 발생: {}", ex.getMessage());
        
        // 스택 트레이스 로깅
        String stackTrace = Arrays.stream(ex.getStackTrace())
                .limit(10)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        log.error("Stack trace:\n{}", stackTrace);
        
        // 컴파일러 관련 에러인지 확인
        boolean isCompilerRelated = isCompilerRelated(ex);
        String errorMessage = isCompilerRelated 
                ? "코드 실행 중 Null 참조 오류가 발생했습니다. 변수의 초기화 상태를 확인하세요."
                : "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.";
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Null Pointer Exception",
                errorMessage,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * MissingRequestValueException 처리
     */
    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestValueException(
            MissingRequestValueException ex, HttpServletRequest request) {
        log.error("MissingRequestValueException 발생: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Missing Required Parameter",
                "필수 파라미터가 누락되었습니다: " + ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * ServletRequestBindingException 처리
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponseDTO> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpServletRequest request) {
        log.error("ServletRequestBindingException 발생: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Request Binding Error",
                "요청 데이터 바인딩 중 오류가 발생했습니다: " + ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * RuntimeException 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("RuntimeException 발생: {}", ex.getMessage());
        log.error("Exception type: {}", ex.getClass().getName());
        
        // 스택 트레이스 로깅
        String stackTrace = Arrays.stream(ex.getStackTrace())
                .limit(10)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        log.error("Stack trace:\n{}", stackTrace);
        
        // 에러 메시지 분석
        String errorMessage = ex.getMessage();
        if (errorMessage == null) {
            errorMessage = "알 수 없는 서버 오류가 발생했습니다.";
        }
        
        // 컴파일러 관련 에러인지 확인 
        boolean isCompilerRelated = isCompilerRelated(ex);
        if (isCompilerRelated) {
            if (errorMessage.contains("';' expected") || errorMessage.contains("missing semicolon")) {
                errorMessage = "코드에 세미콜론(;)이 누락되었습니다. 문장 끝에 세미콜론을 추가해주세요.";
            } else if (errorMessage.contains("cannot find symbol") || errorMessage.contains("undefined variable")) {
                errorMessage = "선언되지 않은 변수나 메서드를 사용했습니다. 변수명이나 메서드명의 오타를 확인하세요.";
            } else if (errorMessage.contains("bracket") || errorMessage.contains("parenthesis")) {
                errorMessage = "코드에 괄호가 일치하지 않습니다. 열린 괄호와 닫힌 괄호의 짝이 맞는지 확인하세요.";
            }
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Runtime Error",
                errorMessage,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 모든 예외 처리 (Throwable)
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(Throwable ex, HttpServletRequest request) {
        log.error("Throwable 발생: {}", ex.getMessage());
        log.error("Exception type: {}", ex.getClass().getName());
        
        // 스택 트레이스 로깅
        String stackTrace = Arrays.stream(ex.getStackTrace())
                .limit(10)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        log.error("Stack trace:\n{}", stackTrace);
        
        // 컴파일러 관련 에러인지 분석
        boolean isCompilerRelated = isCompilerRelated(ex);
        
        // 에러 메시지 생성
        String errorMessage = ex.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "알 수 없는 서버 오류가 발생했습니다.";
        } else if (isCompilerRelated) {
            if (ex instanceof NullPointerException) {
                errorMessage = "코드 실행 중 Null 참조 오류가 발생했습니다. 변수 초기화를 확인하세요.";
            } else if (ex instanceof ArrayIndexOutOfBoundsException) {
                errorMessage = "배열 인덱스 범위 오류가 발생했습니다. 배열 크기와 인덱스를 확인하세요.";
            } else if (errorMessage.contains("compilation") || errorMessage.contains("compile")) {
                errorMessage = "코드 컴파일 중 오류가 발생했습니다. 문법을 확인하세요.";
            }
        }
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Server Error",
                errorMessage,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 예외가 컴파일러 관련인지 확인
     */
    private boolean isCompilerRelated(Throwable ex) {
        // 스택 트레이스 확인
        for (StackTraceElement element : ex.getStackTrace()) {
            String className = element.getClassName();
            if (className.contains("compiler") || className.contains("Compiler")) {
                return true;
            }
        }
        
        // 예외 메시지 확인
        String message = ex.getMessage();
        if (message != null) {
            return message.contains("compilation") || 
                   message.contains("compile") || 
                   message.contains("execution") ||
                   message.contains("runtime") ||
                   message.contains("java.lang.") ||
                   message.contains("code");
        }
        
        return false;
    }
    
    /**
     * 에러 응답 DTO
     */
    private static class ErrorResponseDTO {
        private int status;
        private String error;
        private String message;
        private String path;
        private LocalDateTime timestamp;
        
        public ErrorResponseDTO(int status, String error, String message, String path, LocalDateTime timestamp) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.timestamp = timestamp;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getError() {
            return error;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getPath() {
            return path;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex){
        log.error("Custom exception occurred: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getCustomErrorCode().getStatusCode().value(),
                ex.getCustomErrorCode().getErrorCode(),
                ex.getCustomErrorCode().getMessage(),
                "/",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, ex.getCustomErrorCode().getStatusCode());
    }
}