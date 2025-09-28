package com.lankatrails.lankatrails_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final Map<String, Object> metaData;
    private final String userMessage;

    protected BaseException(HttpStatus status, ErrorCode errorCode,
                            String technicalMessage, String userMessage) {
        this(status, errorCode, technicalMessage, userMessage, null);
    }

    protected BaseException(HttpStatus status, ErrorCode errorCode,
                            String technicalMessage, String userMessage,
                            Map<String, Object> metaData) {
        super(technicalMessage);
        this.status = status;
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.metaData = metaData != null ? metaData : new HashMap<>();
    }
}