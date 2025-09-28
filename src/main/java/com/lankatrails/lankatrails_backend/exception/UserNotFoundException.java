package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, message, "User not found");
    }
}
