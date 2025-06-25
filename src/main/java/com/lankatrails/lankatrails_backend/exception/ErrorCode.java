package com.lankatrails.lankatrails_backend.exception;

public enum ErrorCode {
    // Validation Errors
    VALIDATION_FAILED("VALIDATION_001", "Validation failed"),
    INVALID_EMAIL("VALIDATION_002", "Invalid email format"),

    // Authentication Errors
    AUTHENTICATION_FAILED("AUTH_001", "Authentication failed"),
    BAD_CREDENTIALS("AUTH_002", "Invalid email or password"),
    EMAIL_NOT_VERIFIED("AUTH_003", "Email not verified"),
    USER_NOT_FOUND("AUTH_004", "User not found"),
    UNAUTHORIZED("AUTH_005", "Unauthorized access"),

    // Resource Errors
    RESOURCE_NOT_FOUND("RESOURCE_001", "Resource not found"),
    EMAIL_ALREADY_EXISTS("RESOURCE_002", "Email already in use"),

    // System Errors
    INTERNAL_ERROR("SYSTEM_001", "Internal server error");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}