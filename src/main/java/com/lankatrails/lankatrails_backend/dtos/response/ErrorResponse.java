package com.lankatrails.lankatrails_backend.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lankatrails.lankatrails_backend.exception.BaseException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String userMessage;
    private final String path;
    private final Map<String, Object> details;
    private final Map<String, String> fieldErrors;

    public static ErrorResponse fromBaseException(BaseException exception, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(exception.getStatus().value())
                .code(exception.getErrorCode().name())
                .message(exception.getMessage())
                .userMessage(exception.getUserMessage())
                .path(path)
                .details(exception.getMetaData())
                .build();
    }
}