package com.lankatrails.lankatrails_backend.controller;

import java.security.Principal;
import java.util.List;

import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageDto> getRoomMessages( @PathVariable Long roomId) {
        return chatService.getMessagesForRoom(roomId);
    }

    @GetMapping("/users/{user1Id}/{user2Id}/messages")
    public List<ChatMessageDto> getMessagesBetweenUsers(@PathVariable Long user1Id, @PathVariable Long user2Id) {
        return chatService.getMessagesBetweenUsers(user1Id, user2Id);
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

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<APIResponse<String>> markMessageAsRead(@PathVariable String messageId) {
        APIResponse<String> response = chatService.markMessageAsRead(messageId);
        return ResponseEntity.status(HttpStatus.OK)
                             .body(response);
    }

    @PostMapping("/rooms/{roomId}/read-all")
    public ResponseEntity<APIResponse<String>> markAllMessagesAsRead(@PathVariable Long roomId) {
        APIResponse<String> response = chatService.markAllMessagesAsReadInRoom(roomId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

}

