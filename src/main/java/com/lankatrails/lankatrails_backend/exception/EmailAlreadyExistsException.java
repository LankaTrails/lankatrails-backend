package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String email) {
        super(
                HttpStatus.CONFLICT,                      // Proper status code for duplicate resources
                ErrorCode.EMAIL_ALREADY_EXISTS,          // Specific error code
                "Email already exists in system: " + email, // Technical message (for logs)
                "This email address is already registered", // User-friendly message
                Map.of("email", email)                   // Metadata for debugging
        );
    }
}