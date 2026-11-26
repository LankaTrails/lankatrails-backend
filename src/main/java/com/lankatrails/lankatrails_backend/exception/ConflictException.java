package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

/** 409: the request is valid but cannot be applied to the resource in its current state. */
public class ConflictException extends BaseException {

    public ConflictException(String technicalMessage, String userMessage) {
        this(ErrorCode.CONFLICT, technicalMessage, userMessage);
    }

    public ConflictException(ErrorCode errorCode, String technicalMessage, String userMessage) {
        super(HttpStatus.CONFLICT, errorCode, technicalMessage, userMessage);
    }
}
