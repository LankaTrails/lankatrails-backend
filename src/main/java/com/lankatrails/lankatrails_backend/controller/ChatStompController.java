package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessageDto message, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        // Get user ID from authentication
        Authentication auth = (Authentication) principal;
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        // Verify sender matches authenticated user
        if (!userDetails.getId().equals(message.getSenderId())) {
            throw new UnauthorizedException("Sender ID mismatch");
        }

        chatService.processMessage(message);
    }
}

