package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ChatService {
    void processMessage(ChatMessageDto dto, Long userId, MultipartFile file);

    APIResponse<List<ChatMessageDto>> getMessagesForRoom(Long roomId);

    APIResponse<List<ChatMessageDto>> getMessagesBetweenUsers(Long user1Id, Long user2Id);

    APIResponse<String> markMessageAsRead(String messageId, Long userId);

    APIResponse<String> markAllMessagesAsReadInRoom(Long roomId, Long userId);
}
