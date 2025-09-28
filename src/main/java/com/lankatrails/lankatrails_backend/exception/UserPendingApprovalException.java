package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class UserPendingApprovalException extends BaseException {
    public UserPendingApprovalException(String email) {
        super(
                HttpStatus.FORBIDDEN, // 403 Forbidden is appropriate for unverified email
                ErrorCode.USER_PENDING_APPROVAL, // Custom error code for pending approval
                "User pending approval: " + email, // Technical message (for logs)
                "Your account is pending approval. Please wait for admin confirmation.", // User-friendly message
                Map.of("email", email) // Metadata for debugging
        );
    }
}
