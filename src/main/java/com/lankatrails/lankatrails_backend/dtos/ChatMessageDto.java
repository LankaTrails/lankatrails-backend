package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long chatRoomId;
    private Long senderId;
    private ChatMessageType messageType;
    private String content;
    private Instant sentAt;
}
