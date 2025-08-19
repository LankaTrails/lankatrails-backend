package com.lankatrails.lankatrails_backend.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.dtos.ReadReceiptDto;
import com.lankatrails.lankatrails_backend.dtos.response.WebSocketErrorResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.service.ChatService;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.TypingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatStompController {

    private final ChatService chatService;
    private final TypingService typingService;
    private final ChatRoomService chatRoomService;

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessageDto message, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        Long userId = ((UserDetailsImpl) ((Authentication) principal).getPrincipal()).getId();
        chatService.processMessage(message, userId, null);
    }

    @MessageMapping("/markAsRead")
    public void markMessageAsRead(@Payload ReadReceiptDto readReceiptDto, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        Long userId = ((UserDetailsImpl) ((Authentication) principal).getPrincipal()).getId();
        if (readReceiptDto.getMessageId() != null) {
            // Mark single message as read
            chatService.markMessageAsRead(readReceiptDto.getMessageId(), userId);
        } else if (readReceiptDto.getRoomId() != null) {
            // Mark all messages in room as read
            chatService.markAllMessagesAsReadInRoom(readReceiptDto.getRoomId(), userId);
        } else {
            throw new BadRequestException("Either messageId or roomId must be provided");
        }
    }

    @MessageMapping("/typing/start/room/{roomId}")
    public void startTyping(@DestinationVariable Long roomId, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) ((Authentication) principal).getPrincipal();
        Long userId = userDetails.getId();
        String username = userDetails.getUsername();

        // Validate user is in the room
        if (!chatRoomService.isUserInRoom(userId, roomId)) {
            throw new BadRequestException("User is not part of this chat room");
        }

        typingService.startTyping(roomId, userId, username);
        log.debug("User {} started typing in room {}", userId, roomId);
    }

    @MessageMapping("/typing/stop/room/{roomId}")
    public void stopTyping(@DestinationVariable Long roomId, Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) ((Authentication) principal).getPrincipal();
        Long userId = userDetails.getId();
        String username = userDetails.getUsername();

        // Validate user is in the room
        if (!chatRoomService.isUserInRoom(userId, roomId)) {
            throw new BadRequestException("User is not part of this chat room");
        }
        
        typingService.stopTyping(roomId, userId, username);
        log.debug("User {} stopped typing in room {}", userId, roomId);
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
