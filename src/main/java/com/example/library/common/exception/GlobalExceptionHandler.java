package com.example.library.common.exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .distinct()
                .toList();
        String message = "輸入資料驗證失敗：" + String.join("；", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系統發生未預期錯誤，請稍後再試。", null);
    }

    private String formatFieldError(FieldError fieldError) {
        return switch (fieldError.getField()) {
            case "phoneNumber" -> "手機號碼" + fieldError.getDefaultMessage();
            case "password" -> "密碼" + fieldError.getDefaultMessage();
            case "userName" -> "使用者名稱" + fieldError.getDefaultMessage();
            case "inventoryId" -> "庫存編號" + fieldError.getDefaultMessage();
            default -> fieldError.getField() + fieldError.getDefaultMessage();
        };
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            List<String> errors
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
