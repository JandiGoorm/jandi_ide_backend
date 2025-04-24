package com.webproject.jandi_ide_backend.compiler.exception;

import java.util.Arrays;
import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.webproject.jandi_ide_backend.algorithm.solution.entity.Solution.SolutionStatus;
import com.webproject.jandi_ide_backend.compiler.dto.CompilerErrorResponseDto;
import com.webproject.jandi_ide_backend.compiler.controller.CompilerController;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.extern.slf4j.Slf4j;

/**
 * 컴파일러 관련 예외 처리 클래스.
 * 컴파일러 패키지 내의 모든 예외를 처리하며, 컴파일 오류, 실행 오류 등을
 * 사용자 친화적인 메시지로 변환하여 반환합니다.
 */
@Slf4j
@ControllerAdvice(basePackageClasses = {CompilerController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)  // 가장 높은 우선순위로 설정
public class CompilerExceptionHandler {

    /**
     * CompilerException 처리
     * 컴파일러에서 명시적으로 발생시킨 예외를 처리합니다.
     */
    @ExceptionHandler(CompilerException.class)
    @ApiResponse(
        responseCode = "400",
        description = "컴파일러 예외 발생",
        content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
    )
    @Hidden // API 응답은 컨트롤러의 ApiResponse로 문서화됨
    public ResponseEntity<CompilerErrorResponseDto> handleCompilerException(CompilerException ex) {
        log.error("컴파일러 예외 발생: {}", ex.getMessage(), ex);
        
        CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Compilation Failed")
                .message(ex.getMessage() != null ? ex.getMessage() : "컴파일러 오류가 발생했습니다")
                .timestamp(LocalDateTime.now())
                .errorType(ex.getErrorType() != null ? ex.getErrorType().name() : "COMPILATION_ERROR")
                .errorDetails(ex.getErrorDetails() != null ? ex.getErrorDetails() : "상세 정보가 제공되지 않았습니다")
                .code(ex.getCode())
                .language(ex.getLanguage())
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 컴파일러 관련 모든 Exception 처리
     * CompilerException 이외의 일반 예외를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    @ApiResponse(
        responseCode = "400",
        description = "일반 예외 발생",
        content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
    )
    @Hidden // API 응답은 컨트롤러의 ApiResponse로 문서화됨
    public ResponseEntity<CompilerErrorResponseDto> handleGenericException(Exception ex) {
        log.error("컴파일러 처리 중 예외 발생: {}", ex.getMessage(), ex);
        
        // NullPointerException 특별 처리
        if (ex instanceof NullPointerException) {
            // 스택 트레이스 분석
            StackTraceElement[] stackTrace = ex.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                // CompilerService.submitCode 메서드의 stream 관련 NPE인 경우
                if (stackTrace[0].getClassName().contains("ReferencePipeline") && 
                    containsMethodInStack(stackTrace, "CompilerService", "submitCode")) {
                    
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(CompilerErrorResponseDto.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .error("Compilation Failed")
                            .message("코드에 컴파일 오류가 있습니다")
                            .timestamp(LocalDateTime.now())
                            .errorType(SolutionStatus.COMPILATION_ERROR.name())
                            .errorDetails("코드에 구문 오류(예: 세미콜론 누락, 괄호 불일치)가 있어 컴파일에 실패했습니다. " + 
                                        "코드를 자세히 검토해 주세요.\n" +
                                        "특히 세미콜론(;)이 모든 문장 끝에 있는지 확인하세요.")
                            .build());
                }
            }
        }
        
        // 스택 트레이스의 첫 번째 요소를 상세 정보로 추가
        StackTraceElement[] stackTrace = ex.getStackTrace();
        String details = "추가 정보 없음";
        if (stackTrace != null && stackTrace.length > 0) {
            details = "위치: " + stackTrace[0].getClassName() + "." + 
                     stackTrace[0].getMethodName() + " (라인: " + stackTrace[0].getLineNumber() + ")";
        }
        
        // 오류 메시지 분석하여 사용자 친화적 메시지 생성
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "알 수 없는 오류가 발생했습니다";
        String errorDetails = details;
        String errorType = "RUNTIME_ERROR";
        
        // 유형별 오류 처리
        if (ex instanceof NullPointerException) {
            errorMessage = "코드 실행 중 Null 참조 오류가 발생했습니다";
            errorDetails = "코드에서 초기화되지 않은 객체나 변수를 사용했을 수 있습니다. " +
                          "변수 초기화를 확인하고, 입력값 처리 로직이 올바른지 검토해 주세요.";
            errorType = SolutionStatus.RUNTIME_ERROR.name();
        } else if (ex instanceof ArrayIndexOutOfBoundsException) {
            errorMessage = "코드 실행 중 배열 인덱스 범위 오류가 발생했습니다";
            errorDetails = "배열이나 리스트의 범위를 벗어난 인덱스에 접근했습니다. " +
                          "배열 크기와 접근하는 인덱스 값을 확인해 주세요.";
            errorType = SolutionStatus.RUNTIME_ERROR.name();
        } else if (ex instanceof NumberFormatException) {
            errorMessage = "숫자 변환 오류가 발생했습니다";
            errorDetails = "숫자가 아닌 문자열을 숫자로 변환하려고 시도했습니다. " +
                          "입력값이 올바른 형식인지 확인하고, 예외 처리를 추가해 주세요.";
            errorType = SolutionStatus.RUNTIME_ERROR.name();
        }
        
        // 컴파일 관련 오류 확인
        else if (errorMessage.contains("';' expected") || errorMessage.contains("missing semicolon")) {
            errorDetails = "코드에 세미콜론(;)이 누락되었습니다. 문장 끝에 세미콜론을 추가해주세요.\n" + details;
            errorType = SolutionStatus.COMPILATION_ERROR.name();
            errorMessage = "컴파일 오류: 세미콜론 누락";
        } else if (errorMessage.contains("bracket") || errorMessage.contains("parenthesis") || 
                errorMessage.contains("expecting") || errorMessage.contains("unclosed")) {
            errorDetails = "코드에 괄호가 일치하지 않습니다. 열린 괄호와 닫힌 괄호의 짝이 맞는지 확인해주세요.\n" + details;
            errorType = SolutionStatus.COMPILATION_ERROR.name();
            errorMessage = "컴파일 오류: 괄호 불일치";
        } else if (errorMessage.contains("cannot find symbol") || errorMessage.contains("undefined variable")) {
            errorDetails = "선언되지 않은 변수나 메서드를 사용했습니다. 변수명이나 메서드명의 오타를 확인하고, 사용 전에 선언했는지 확인하세요.\n" + details;
            errorType = SolutionStatus.COMPILATION_ERROR.name();
            errorMessage = "컴파일 오류: 심볼을 찾을 수 없음";
        } else if (errorMessage.contains("incompatible types") || errorMessage.contains("cannot convert")) {
            errorDetails = "타입 불일치 오류입니다. 변수 타입과 할당하려는 값의 타입이 호환되는지 확인하세요.\n" + details;
            errorType = SolutionStatus.COMPILATION_ERROR.name();
            errorMessage = "컴파일 오류: 타입 불일치";
        } else if (isRuntimeError(errorMessage)) {
            errorType = SolutionStatus.RUNTIME_ERROR.name();
            errorMessage = "런타임 오류 발생";
        }
        
        // 응답 생성
        CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Compilation or Runtime Error")
                .message(errorMessage)
                .timestamp(LocalDateTime.now())
                .errorType(errorType)
                .errorDetails(errorDetails)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 컴파일러 관련 모든 예외 처리 (Throwable)
     * Exception으로 캐치되지 않는 모든 예외를 처리합니다.
     */
    @ExceptionHandler(Throwable.class)
    @ApiResponse(
        responseCode = "400",
        description = "예상치 못한 오류 발생",
        content = @Content(schema = @Schema(implementation = CompilerErrorResponseDto.class))
    )
    @Hidden // API 응답은 컨트롤러의 ApiResponse로 문서화됨
    public ResponseEntity<CompilerErrorResponseDto> handleAllThrowable(Throwable ex) {
        log.error("컴파일러 처리 중 예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        
        // 오류의 근본 원인 추적
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        
        // 스택 트레이스 정보 생성
        String stackTraceInfo = "";
        if (ex.getStackTrace() != null && ex.getStackTrace().length > 0) {
            stackTraceInfo = Arrays.stream(ex.getStackTrace())
                .limit(3) // 처음 3개 요소만 사용
                .map(StackTraceElement::toString)
                .reduce("", (a, b) -> a + "\n" + b);
        }
        
        String details = "오류 유형: " + ex.getClass().getName();
        if (rootCause != ex) {
            details += ", 근본 원인: " + rootCause.getClass().getName() + 
                      (rootCause.getMessage() != null ? " - " + rootCause.getMessage() : "");
        }
        
        if (!stackTraceInfo.isEmpty()) {
            details += "\n스택 트레이스:" + stackTraceInfo;
        }
        
        // 메시지 분석하여 오류 유형 결정
        String errorType = determineErrorType(ex);
        String userMessage = getUserFriendlyMessage(ex, errorType);
        
        CompilerErrorResponseDto errorResponse = CompilerErrorResponseDto.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Code Execution Error")
                .message(userMessage)
                .timestamp(LocalDateTime.now())
                .errorType(errorType)
                .errorDetails(details)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 오류 메시지를 분석하여 런타임 오류인지 확인
     */
    private boolean isRuntimeError(String errorMessage) {
        if (errorMessage == null) return false;
        
        return errorMessage.contains("ArrayIndexOutOfBoundsException") ||
               errorMessage.contains("NullPointerException") ||
               errorMessage.contains("IllegalArgumentException") ||
               errorMessage.contains("ClassCastException") ||
               errorMessage.contains("ArithmeticException") ||
               errorMessage.contains("OutOfMemoryError") ||
               errorMessage.contains("StackOverflowError");
    }
    
    /**
     * 예외 유형을 결정
     */
    private String determineErrorType(Throwable ex) {
        String message = ex.getMessage();
        if (message == null) message = "";
        
        // 컴파일 오류 관련 키워드
        if (message.contains("compile") || message.contains("syntax") || 
            message.contains("expected") || message.contains("missing") ||
            message.contains("cannot find") || message.contains("incompatible")) {
            return SolutionStatus.COMPILATION_ERROR.name();
        }
        
        // 런타임 오류 관련 키워드
        if (message.contains("runtime") || message.contains("execution") ||
            message.contains("NullPointer") || message.contains("ArrayIndexOutOfBounds") ||
            message.contains("ClassCast") || message.contains("Arithmetic")) {
            return SolutionStatus.RUNTIME_ERROR.name();
        }
        
        // 시간 초과 관련 키워드
        if (message.contains("timeout") || message.contains("timed out") ||
            message.contains("시간 초과")) {
            return SolutionStatus.TIMEOUT.name();
        }
        
        // 메모리 초과 관련 키워드
        if (message.contains("memory") || message.contains("OutOfMemory") ||
            message.contains("메모리 초과")) {
            return SolutionStatus.MEMORY_LIMIT.name();
        }
        
        // 기본값
        return "SERVER_ERROR";
    }
    
    /**
     * 사용자 친화적인 메시지 생성
     */
    private String getUserFriendlyMessage(Throwable ex, String errorType) {
        if (SolutionStatus.COMPILATION_ERROR.name().equals(errorType)) {
            return "코드에 컴파일 오류가 있습니다. 문법을 확인해주세요.";
        } else if (SolutionStatus.RUNTIME_ERROR.name().equals(errorType)) {
            return "코드 실행 중 오류가 발생했습니다. 잘못된 입력이나 예외 상황을 확인해주세요.";
        } else if (SolutionStatus.TIMEOUT.name().equals(errorType)) {
            return "코드 실행 시간이 제한을 초과했습니다. 알고리즘을 최적화하거나 무한 루프를 확인해주세요.";
        } else if (SolutionStatus.MEMORY_LIMIT.name().equals(errorType)) {
            return "코드 실행 중 메모리 사용량이 제한을 초과했습니다. 메모리 사용을 최적화해주세요.";
        } else {
            return ex.getMessage() != null ? ex.getMessage() : "예상치 못한 오류가 발생했습니다.";
        }
    }

    /**
     * 스택 트레이스에서 특정 클래스와 메서드가 포함되어 있는지 확인
     */
    private boolean containsMethodInStack(StackTraceElement[] stackTrace, String className, String methodName) {
        if (stackTrace == null) return false;
        
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains(className) && 
                element.getMethodName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
} 