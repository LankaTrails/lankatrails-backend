package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class LicenseExpiredException extends BaseException{
    public LicenseExpiredException(String expiryMessage) {
        super(
                HttpStatus.FORBIDDEN,                      // Proper status code for duplicate resources
                ErrorCode.LICENSE_EXPIRED,          // Specific error code
                "License Expired ", // Technical message (for logs)
                "License has Expired.Renew to Continue", // User-friendly message
                Map.of("LicenseExpiryMessage", expiryMessage)                   // Metadata for debugging
        );
    }
}
