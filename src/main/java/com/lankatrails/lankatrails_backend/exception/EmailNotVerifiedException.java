package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class EmailNotVerifiedException extends BaseException {

    public EmailNotVerifiedException(String email) {
        super(
                HttpStatus.FORBIDDEN, // 403 Forbidden is appropriate for unverified email
                ErrorCode.EMAIL_NOT_VERIFIED, // Custom error code for unverified email
                "Email not verified: " + email, // Technical message (for logs)
                "Please verify your email address to proceed", // User-friendly message
                Map.of("email", email) // Metadata for debugging
        );
    }
}
