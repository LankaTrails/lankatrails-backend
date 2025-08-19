package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingStateDto {
    private Long roomId;
    private Long userId;
    private String username;
    private boolean isTyping;
    private Instant timestamp;
}
