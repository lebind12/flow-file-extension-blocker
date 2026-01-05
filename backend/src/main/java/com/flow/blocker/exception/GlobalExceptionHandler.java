package com.flow.blocker.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExtensionException.class)
    public ResponseEntity<ErrorResponse> handleExtensionException(ExtensionException e) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .code(e.getErrorCode().name())
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "유효성 검사 실패";

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.builder()
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .code("INTERNAL_ERROR")
                        .message("서버 오류가 발생했습니다.")
                        .build());
    }

    @Getter
    @Builder
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
