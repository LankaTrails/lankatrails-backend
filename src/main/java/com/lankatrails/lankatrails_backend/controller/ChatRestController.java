package com.lankatrails.lankatrails_backend.controller;

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

    @PostMapping("/{roomId}/send-file")
    public void sendFileMessage(@PathVariable Long roomId,
                                @RequestPart("message") ChatMessageDto message,
                                @RequestPart("file") MultipartFile file,
                                Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Long userId = ((UserDetailsImpl) ((Authentication) principal).getPrincipal()).getId();

        chatService.processMessage(message, userId, file);
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<APIResponse<String>> markMessageAsRead(@PathVariable String messageId) {
        APIResponse<String> response = chatService.markMessageAsRead(messageId, null);
        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    @PutMapping("/rooms/{roomId}/read-all")
    public ResponseEntity<APIResponse<String>> markAllMessagesAsRead(@PathVariable Long roomId) {
        APIResponse<String> response = chatService.markAllMessagesAsReadInRoom(roomId, null);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }
}

