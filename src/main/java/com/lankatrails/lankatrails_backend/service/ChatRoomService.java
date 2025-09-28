package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.DirectChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.GroupChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.Trip;

import java.util.List;

public interface ChatRoomService {
    APIResponse<DirectChatRoomDto> getDirectChatRoom(Long providerId);

    APIResponse<List<DirectChatRoomDto>> getMyDirectChatRooms();

    APIResponse<ChatRoomDto> getChatRoomById(Long chatRoomId);

    Boolean isUserInRoom(Long userId, Long chatRoomId);

    GroupChatRoomDto setChatRoomForTrip(Trip trip);

    APIResponse<GroupChatRoomDto> getGroupChatRoomByTripId(Long tripId);
}
