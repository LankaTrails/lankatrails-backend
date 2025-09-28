package com.lankatrails.lankatrails_backend.exception;

public class FileUploadException extends BaseException {
    public FileUploadException(String message) {
        super(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, // Proper status code for server errors
                ErrorCode.FILE_UPLOAD_ERROR, // Specific error code for file upload issues
                message, // Technical message (for logs)
                "File upload failed" // User-friendly message
        );
    }
}
