package com.lankatrails.lankatrails_backend.controller;

import org.springframework.http.MediaType;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.security.Principal;
import java.util.List;

import com.lankatrails.lankatrails_backend.dtos.TypingStateDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.service.TypingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final TypingService typingService;
    private final AuthUtils authUtils;
    private final ObjectMapper objectMapper;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<APIResponse<List<ChatMessageDto>>> getRoomMessages( @PathVariable Long roomId) {
        APIResponse<List<ChatMessageDto>> response = chatService.getMessagesForRoom(roomId);
        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    @GetMapping("/users/{user1Id}/{user2Id}/messages")
    public ResponseEntity<APIResponse<List<ChatMessageDto>>> getMessagesBetweenUsers(@PathVariable Long user1Id, @PathVariable Long user2Id) {
        APIResponse<List<ChatMessageDto>> response = chatService.getMessagesBetweenUsers(user1Id, user2Id);
        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    // Mobile posts to /chat/{roomId}/send-file with a JSON "message" part; the provider dashboard
    // posts to /chat/rooms/{roomId}/send-file with a plain-text "messageData" part. Both are accepted.
    @PostMapping(value = {"/{roomId}/send-file", "/rooms/{roomId}/send-file"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ChatMessageDto>> sendFileMessage(
            @PathVariable Long roomId,
            @RequestPart(value = "message", required = false) String message,
            // A plain FormData string field, which the servlet container exposes as a parameter
            @RequestParam(value = "messageData", required = false) String messageData,
            @RequestPart("file") MultipartFile file) {
        String json = message != null ? message : messageData;
        if (json == null || json.isBlank()) {
            throw new BadRequestException("A 'message' (or 'messageData') part describing the file is required");
        }
        ChatMessageDto dto;
        try {
            dto = objectMapper.readValue(json, ChatMessageDto.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid message JSON: " + e.getOriginalMessage());
        }
        dto.setChatRoomId(roomId); // the path decides the room

        chatService.processMessage(dto, authUtils.loggedInUserId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<ChatMessageDto>builder()
                .success(true)
                .message("File sent")
                .data(dto)
                .build());
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<APIResponse<String>> markMessageAsRead(@PathVariable String messageId) {
        APIResponse<String> response = chatService.markMessageAsRead(messageId, authUtils.loggedInUserId());
        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    @PutMapping("/rooms/{roomId}/read-all")
    public ResponseEntity<APIResponse<String>> markAllMessagesAsRead(@PathVariable Long roomId) {
        APIResponse<String> response = chatService.markAllMessagesAsReadInRoom(roomId, authUtils.loggedInUserId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}

