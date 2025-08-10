package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    private String id;

    private Long chatRoomId;

    private Long senderId;

    @Enumerated(EnumType.STRING)
    private ChatMessageType messageType;

    private String content; // text or URL

    private Instant sentAt = Instant.now();

//    private ServiceCard serviceCard; // nullable, for SERVICE_CARD messageType

//    private List<Attachment> attachments; // nullable, for images/files

    // Optional fields for delivery/read receipts etc.
}

