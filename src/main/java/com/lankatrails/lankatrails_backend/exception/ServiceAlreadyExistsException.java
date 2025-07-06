package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ServiceAlreadyExistsException extends BaseException{
    public ServiceAlreadyExistsException(Long id) {
        super(
                HttpStatus.CONFLICT,                      // Proper status code for duplicate resources
                ErrorCode.SERVICE_ALREADY_EXISTS,          // Specific error code
                "Service already exists in system: " + id, // Technical message (for logs)
                "This service is already registered", // User-friendly message
                Map.of("serviceId", id)                   // Metadata for debugging
        );
    }

}
