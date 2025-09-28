package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends BaseException {

    public BadCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.BAD_CREDENTIALS, message, "Invalid email or password");
    }

    public BadCredentialsException(String message, String userMessage) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.BAD_CREDENTIALS, message, userMessage);
    }


//    public BadCredentialsException(String message, Throwable cause) {
//        super(HttpStatus.UNAUTHORIZED, ErrorCode.BAD_CREDENTIALS, message, "Invalid email or password", cause);
//    }
}
