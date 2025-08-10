package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat-rooms")
public class ChatRoomController {
    @Autowired
    ChatRoomService chatRoomService;

    @PostMapping("/create")
    public APIResponse<ChatRoomDto> createChatRoom(@Valid @RequestBody ChatRoomDto chatRoomDto) {
        log.info("Creating chat room with details: {}", chatRoomDto);
        APIResponse<ChatRoomDto> response = chatRoomService.createChatRoom(chatRoomDto);
        log.info("Chat room created successfully: {}", response.getData());
        return response;
    }

    @GetMapping("/{id}")
    public APIResponse<ChatRoomDto> getChatRoomById(@PathVariable Long roomId) {
        log.info("Fetching chat room with ID: {}", roomId);
        APIResponse<ChatRoomDto> response = chatRoomService.getChatRoomById(roomId);
        log.info("Fetched chat room: {}", response.getData());
        return response;
    }

    @PostMapping("/get-by-type-and-participants")
    public APIResponse<ChatRoomDto> getChatRoomByTypeAndParticipants(ChatRoomDto chatRoomDto) {
        log.info("Fetching chat room by type and participants: {}", chatRoomDto);
        APIResponse<ChatRoomDto> response = chatRoomService.getChatRoomByTypeAndParticipants(chatRoomDto);
        log.info("Fetched chat room: {}", response.getData());
        return response;
    }
}
