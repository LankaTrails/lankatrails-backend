package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;

public interface ChatService {
    public void processMessage(ChatMessageDto dto);
}
