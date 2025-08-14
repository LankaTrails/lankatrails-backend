package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
    private String replyToMessageId; // nullable, for replies
    private Long serviceCardId; // nullable, for SERVICE_CARD messageType
    private ServiceDTO serviceCard; // nullable, for SERVICE_CARD messageType
    private ChatFilesDto files; // nullable, for file attachments
}
