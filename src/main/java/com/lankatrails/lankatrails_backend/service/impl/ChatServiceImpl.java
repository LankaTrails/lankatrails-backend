package com.lankatrails.lankatrails_backend.service.impl;

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

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRoomService chatRoomService;

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
}
