package com.lankatrails.lankatrails_backend.service;

public interface TypingService {
    void startTyping(Long roomId, Long userId, String username);

    void stopTyping(Long roomId, Long userId, String username);
}
