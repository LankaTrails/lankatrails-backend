package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.Trip;

import java.util.List;

public interface ChatRoomService {
    APIResponse<ChatRoomDto> getDirectChatRoom(Long userId);
    APIResponse<List<ChatRoomDto>> getMyChatRooms();
    APIResponse<ChatRoomDto> getChatRoomById(Long chatRoomId);
    Boolean isUserInRoom(Long userId, Long chatRoomId);
    ChatRoomDto mapToDto(ChatRoom chatRoom);
    ChatRoomDto setChatRoomForTrip(Trip trip);
}
