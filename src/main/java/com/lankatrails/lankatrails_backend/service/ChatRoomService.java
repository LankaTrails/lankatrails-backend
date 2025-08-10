package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

public interface ChatRoomService {
    APIResponse<ChatRoomDto> createChatRoom(ChatRoomDto chatRoomDto);
    APIResponse<ChatRoomDto> getChatRoomById(Long chatRoomId);
    APIResponse<ChatRoomDto> getChatRoomByTypeAndParticipants(ChatRoomDto chatRoomDto);
    Boolean isUserInRoom(Long userId, Long chatRoomId);
}
