package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByParticipants_UserIdAndParticipants_UserIdAndChatRoomType(Long user1Id, Long user2Id, ChatRoomType chatRoomType);

    Boolean existsByRoomIdAndParticipants_UserId(Long roomId, Long userId);

    @Query("""
    SELECT cr FROM ChatRoom cr
    WHERE cr.chatRoomType = :chatRoomType
      AND EXISTS (
        SELECT 1 FROM cr.participants p1 WHERE p1.userId = :user1Id
      )
      AND EXISTS (
        SELECT 1 FROM cr.participants p2 WHERE p2.userId = :user2Id
      )
""")
    Optional<ChatRoom> findDirectRoomBetweenUsers(
            Long user1Id,
            Long user2Id,
            ChatRoomType chatRoomType
    );

    Optional<ChatRoom> findByTrip_TripId(Long tripId);
}
