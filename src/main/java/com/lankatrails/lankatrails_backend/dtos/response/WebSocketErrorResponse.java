package com.lankatrails.lankatrails_backend.dtos.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketErrorResponse {
    private Instant timestamp;
    private String error;
    private String message;
    private String userMessage;
    private String type;

    public static WebSocketErrorResponse create(String error, String message, String userMessage, String type) {
        return WebSocketErrorResponse.builder()
                .timestamp(Instant.now())
                .error(error)
                .message(message)
                .userMessage(userMessage)
                .type(type)
                .build();
    }
}
