package com.webproject.jandi_ide_backend.global;

import com.webproject.jandi_ide_backend.global.error.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * 전역 예외 처리 클래스.
 * 애플리케이션 전반에서 발생하는 RuntimeException(및 그 하위 예외)을 잡아서
 * 클라이언트에게 표준화된 에러 응답을 보내는 역할을 함.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex) {
       ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                LocalDateTime.now().toString()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex){
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                ex.getCustomErrorCode().getStatusCode().value(),
                ex.getCustomErrorCode().getErrorCode(),
                ex.getCustomErrorCode().getMessage(),
                ex.getCustomErrorCode().getTimestamp()
        );

        return new ResponseEntity<>(errorResponse, ex.getCustomErrorCode().getStatusCode());
    }
}