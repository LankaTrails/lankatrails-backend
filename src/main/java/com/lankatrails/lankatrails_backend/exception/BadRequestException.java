package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BadRequestException extends BaseException{
    public BadRequestException(String message, String resourceName, Long resourceId) {
        super(
                HttpStatus.BAD_REQUEST, // Proper status code for bad requests
                ErrorCode.BAD_REQUEST, // Specific error code
                message, // Technical message (for logs)
                "The request was invalid or cannot be served", // User-friendly message
                Map.of("resourceName", resourceName, "resourceId", resourceId) // Metadata for debugging
        );
    }

}
