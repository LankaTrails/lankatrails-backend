package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByParticipants_UserIdAndParticipants_UserIdAndChatRoomType(Long user1Id, Long user2Id, ChatRoomType chatRoomType);
    Boolean existsByRoomIdAndParticipants_UserId(Long roomId, Long userId);
}
