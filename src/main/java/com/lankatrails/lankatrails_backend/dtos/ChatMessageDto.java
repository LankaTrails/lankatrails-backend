package com.lankatrails.lankatrails_backend.dtos;

import java.time.Instant;
import java.util.Map;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private String id; // Message ID for read receipts
    private Long chatRoomId;
    private Long senderId;
    private ChatMessageType messageType;
    private String content;
    private Instant sentAt;
    private String replyToMessageId; // nullable, for replies
    private Long serviceCardId; // nullable, for SERVICE_CARD messageType
    private ServiceDTO serviceCard; // nullable, for SERVICE_CARD messageType
    private ChatFilesDto files; // nullable, for file attachments
    private Map<Long, Instant> readBy; // userId -> timestamp when read
}
