package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.lankatrails.lankatrails_backend.config.RabbitConfig;
import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.model.ChatMessage;
import com.lankatrails.lankatrails_backend.repositories.MessageRepository;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import com.lankatrails.lankatrails_backend.service.ChatService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRoomService chatRoomService;
    private final AuthUtils authUtils;

    @Override
    public void processMessage(ChatMessageDto dto, Long userId) {
        // Check if user is part of the chat room
        if (!chatRoomService.isUserInRoom(userId, dto.getChatRoomId())) {
            throw new BadRequestException("User is not part of the chat room");
        }

        dto.setSenderId(userId);

        ChatMessage msg = ChatMessage.builder()
                .chatRoomId(dto.getChatRoomId())
                .senderId(dto.getSenderId()) // Use provided senderId
                .messageType(dto.getMessageType())
                .content(dto.getContent())
                .sentAt(dto.getSentAt())
                .build();

        messageRepository.save(msg);
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "chat.room." + dto.getChatRoomId(),
                dto
        );
    }

    public List<ChatMessageDto> getMessagesForRoom(Long roomId) {
        if (!chatRoomService.isUserInRoom(authUtils.loggedInUserId(), roomId)) {
            throw new BadRequestException("User is not part of this chat room");
        }

        return messageRepository.findByChatRoomIdOrderBySentAtAsc(roomId)
                .stream()
                .map(msg -> ChatMessageDto.builder()
                        .chatRoomId(msg.getChatRoomId())
                        .senderId(msg.getSenderId())
                        .messageType(msg.getMessageType())
                        .content(msg.getContent())
                        .sentAt(msg.getSentAt())
                        .build()
                )
                .toList();
    }
}
