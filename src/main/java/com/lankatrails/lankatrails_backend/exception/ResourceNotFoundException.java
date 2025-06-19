package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resourceName, Long resourceId) {
        super(
                HttpStatus.NOT_FOUND, // Proper status code for not found resources
                ErrorCode.RESOURCE_NOT_FOUND, // Specific error code
                "Resource not found: " + resourceName + " with ID " + resourceId, // Technical message (for logs)
                "The requested resource could not be found", // User-friendly message
                Map.of("resourceName", resourceName, "resourceId", resourceId) // Metadata for debugging
        );
    }
}
