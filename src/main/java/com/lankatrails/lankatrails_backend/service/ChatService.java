package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    void processMessage(ChatMessageDto dto, Long userId, MultipartFile file);
    List<ChatMessageDto> getMessagesForRoom(Long roomId);
    List<ChatMessageDto> getMessagesBetweenUsers(Long user1Id, Long user2Id);
}
