package com.lankatrails.lankatrails_backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Validation Errors
    VALIDATION_FAILED("VALIDATION_001", "Validation failed"),
    INVALID_EMAIL("VALIDATION_002", "Invalid email format"),
    ILLEGAL_ARGUMENT("VALIDATION_003", "Illegal argument provided"),

    // Authentication Errors
    AUTHENTICATION_FAILED("AUTH_001", "Authentication failed"),
    BAD_CREDENTIALS("AUTH_002", "Invalid email or password"),
    EMAIL_NOT_VERIFIED("AUTH_003", "Email not verified"),
    USER_NOT_FOUND("AUTH_004", "User not found"),
    UNAUTHORIZED("AUTH_005", "Unauthorized access"),
    USER_PENDING_APPROVAL("AUTH_006", "User pending approval"),

    // Resource Errors
    RESOURCE_NOT_FOUND("RESOURCE_001", "Resource not found"),
    EMAIL_ALREADY_EXISTS("RESOURCE_002", "Email already in use"),
    BAD_REQUEST("RESOURCE_003", "Bad request"),
    SERVICE_ALREADY_EXISTS("RESOURCE_004", "Service already in use"),
    POLICY_HEADING_ALREADY_EXISTS("RESOURCE_005", "Policy heading already exists"),
    LICENSE_EXPIRED("RESOURCE_006", "License has Expired"),
    // System Errors
    INTERNAL_ERROR("SYSTEM_001", "Internal server error"),

    // File Upload Errors
    FILE_UPLOAD_ERROR("FILE_001", "File upload failed");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

}