package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.Trip;

public interface ChatRoomService {
    APIResponse<ChatRoomDto> getChatRoom(ChatRoomDto chatRoomDto);
    APIResponse<ChatRoomDto> getChatRoomById(Long chatRoomId);
    APIResponse<ChatRoomDto> getChatRoomByTypeAndParticipants(ChatRoomDto chatRoomDto);
    APIResponse<ChatRoomDto> getChatRoomByTripId(Long tripId);
    Boolean isUserInRoom(Long userId, Long chatRoomId);
    ChatRoomDto mapToDto(ChatRoom chatRoom);
    void setChatRoomForTrip(Trip trip);
}
