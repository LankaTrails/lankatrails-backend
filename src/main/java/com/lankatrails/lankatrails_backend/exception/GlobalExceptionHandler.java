package com.lankatrails.lankatrails_backend.exception;

import com.lankatrails.lankatrails_backend.dtos.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, WebRequest request) {
        log.warn("Business exception occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.fromBaseException(ex, getRequestPath(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {}", errors);

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.VALIDATION_FAILED.name())
                        .message("Validation failed")
                        .userMessage("Please check your input data")
                        .path(getRequestPath(request))
                        .fieldErrors(errors)
                        .build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code(ErrorCode.VALIDATION_FAILED.name())
                        .message("Constraint violation")
                        .userMessage("Invalid request parameters")
                        .path(getRequestPath(request))
                        .fieldErrors(errors)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code(ErrorCode.INTERNAL_ERROR.name())
                        .message("Internal server error")
                        .userMessage("Something went wrong. Please try again later.")
                        .path(getRequestPath(request))
                        .build()
        );
    }

    private String getRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}