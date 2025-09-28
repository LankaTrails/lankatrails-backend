package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    // Find messages in a room not sent by userId and not read by userId
    @Query("{ 'chatRoomId': ?0, 'senderId': { $ne: ?1 }, $or: [ { 'readBy': { $exists: false } }, { 'readBy.?2': { $exists: false } } ] }")
    List<ChatMessage> findUnreadMessagesInRoomForUser(Long chatRoomId, Long userId, String userIdStr);
}

