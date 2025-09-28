package com.lankatrails.lankatrails_backend.exception;

import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, String resourceName, Long resourceId) {
        super(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                message,
                "The request was invalid or cannot be served",
                buildMetadata(resourceName, resourceId)
        );
    }

    public BadRequestException(String message) {
        super(
                HttpStatus.BAD_REQUEST,
                ErrorCode.BAD_REQUEST,
                message,
                "The request was invalid or cannot be served",
                null
        );
    }

    private static Map<String, Object> buildMetadata(String resourceName, Long resourceId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (resourceName != null) {
            metadata.put("resourceName", resourceName);
        }
        if (resourceId != null) {
            metadata.put("resourceId", resourceId);
        }
        return metadata;
    }


}
