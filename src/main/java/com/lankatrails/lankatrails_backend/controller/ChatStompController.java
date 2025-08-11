package com.lankatrails.lankatrails_backend.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.dtos.response.WebSocketErrorResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatStompController {

    private final ChatService chatService;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessageDto message, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Long userId = ((UserDetailsImpl) ((Authentication) principal).getPrincipal()).getId();
        chatService.processMessage(message, userId);
    }

    @MessageExceptionHandler(BadRequestException.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleBadRequestException(BadRequestException ex, Principal principal) {
        log.warn("BadRequestException in WebSocket message for user {}: {}", 
                 principal != null ? principal.getName() : "UNKNOWN", ex.getMessage());
        return WebSocketErrorResponse.create(
                "BAD_REQUEST",
                ex.getMessage(),
                "Invalid request: " + ex.getMessage(),
                "chat_error"
        );
    }

    @MessageExceptionHandler(UnauthorizedException.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleUnauthorizedException(UnauthorizedException ex, Principal principal) {
        log.warn("UnauthorizedException in WebSocket message for user {}: {}", 
                 principal != null ? principal.getName() : "UNKNOWN", ex.getMessage());
        return WebSocketErrorResponse.create(
                "UNAUTHORIZED",
                ex.getMessage(),
                "You are not authorized to perform this action",
                "auth_error"
        );
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public WebSocketErrorResponse handleGenericException(Exception ex, Principal principal) {
        log.error("Unhandled exception in WebSocket message for user {}: {}", 
                  principal != null ? principal.getName() : "UNKNOWN", ex.getMessage(), ex);
        return WebSocketErrorResponse.create(
                "INTERNAL_ERROR",
                ex.getMessage(),
                "An unexpected error occurred. Please try again.",
                "system_error"
        );
    }
}
