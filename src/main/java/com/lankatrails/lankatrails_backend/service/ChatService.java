package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;

import java.util.List;

public interface ChatService {
    void processMessage(ChatMessageDto dto, Long userId);
    List<ChatMessageDto> getMessagesForRoom(Long roomId);
}
