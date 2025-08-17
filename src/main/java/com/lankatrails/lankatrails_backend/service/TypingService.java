package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.TypingStateDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.util.List;

public interface TypingService {
    void startTyping(Long roomId, Long userId, String username);
    void stopTyping(Long roomId, Long userId, String username);
}
