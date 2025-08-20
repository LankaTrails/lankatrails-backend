package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.DirectChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {
    Optional<DirectChatRoom> findByProvider_UserIdAndTourist_UserId(Long providerId, Long touristId);
    Set<DirectChatRoom> findByTourist_UserId(Long touristId);
    Set<DirectChatRoom> findByProvider_UserId(Long providerId);
    Boolean existsByProvider_UserIdAndTourist_UserId(Long providerId, Long touristId);
}
