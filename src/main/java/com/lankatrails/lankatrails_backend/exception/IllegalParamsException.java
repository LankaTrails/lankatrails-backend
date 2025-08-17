package com.lankatrails.lankatrails_backend.exception;

public class IllegalParamsException extends BaseException {

    public IllegalParamsException(String message) {
        super(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                ErrorCode.ILLEGAL_ARGUMENT,
                message,
                "The provided argument is not valid"
        );
    }
}
