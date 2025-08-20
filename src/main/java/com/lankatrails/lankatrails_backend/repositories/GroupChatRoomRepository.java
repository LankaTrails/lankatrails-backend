package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.GroupChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    Boolean existsByTrip_TripId(Long tripId);
    GroupChatRoom findByTrip_TripId(Long tripId);
}
