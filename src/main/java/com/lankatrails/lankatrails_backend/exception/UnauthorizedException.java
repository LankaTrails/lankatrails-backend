package com.lankatrails.lankatrails_backend.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(
                org.springframework.http.HttpStatus.UNAUTHORIZED, // Proper status code for unauthorized access
                ErrorCode.UNAUTHORIZED, // Specific error code
                message, // Technical message (for logs)
                "Unauthorized access" // User-friendly message
        );
    }
}
