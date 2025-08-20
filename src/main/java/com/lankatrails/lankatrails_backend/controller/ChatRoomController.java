package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.DirectChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    @Autowired
    ChatRoomService chatRoomService;

    @GetMapping("/direct/{userId}")
    public ResponseEntity<APIResponse<DirectChatRoomDto>> getDirectChatRoom(@PathVariable Long userId) {
        log.info("Creating direct chat room with user ID: {}", userId);
        APIResponse<DirectChatRoomDto> response = chatRoomService.getDirectChatRoom(userId);
        log.info("Chat room created successfully: {}", response.getData());
        return ResponseEntity.status(HttpStatus.FOUND)
                .body(response);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<APIResponse<ChatRoomDto>> getChatRoomById(@PathVariable Long roomId) {
        log.info("Fetching chat room with ID: {}", roomId);
        APIResponse<ChatRoomDto> response = chatRoomService.getChatRoomById(roomId);
        log.info("Fetched chat room: {}", response.getData());
        return ResponseEntity.status(HttpStatus.FOUND)
                .body(response);
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<APIResponse<List<DirectChatRoomDto>>> getMyChatRooms() {
        log.info("Fetching all chat rooms for the authenticated user");
        APIResponse<List<DirectChatRoomDto>> response = chatRoomService.getMyDirectChatRooms();
        log.info("Fetched {} chat rooms", response.getData().size());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}
