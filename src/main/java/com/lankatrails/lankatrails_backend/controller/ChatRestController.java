package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

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

}

