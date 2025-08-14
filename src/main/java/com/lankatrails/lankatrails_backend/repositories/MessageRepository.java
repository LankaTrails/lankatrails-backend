package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
}

