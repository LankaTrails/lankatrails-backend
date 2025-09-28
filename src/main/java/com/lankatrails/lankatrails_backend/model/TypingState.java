package com.lankatrails.lankatrails_backend.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingState {
    private Long userId;
    private String username;
    private Long roomId;
    private Instant lastActivity;
    private boolean isTyping;

    public boolean isExpired(long timeoutSeconds) {
        return Instant.now().isAfter(lastActivity.plusSeconds(timeoutSeconds));
    }
}
