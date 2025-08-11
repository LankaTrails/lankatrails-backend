package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

