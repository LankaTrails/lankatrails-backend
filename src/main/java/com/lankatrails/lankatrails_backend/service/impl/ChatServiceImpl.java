package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import com.lankatrails.lankatrails_backend.repositories.ChatRoomRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ServiceService;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRoomService chatRoomService;
    private final AuthUtils authUtils;
    private final ChatRoomRepository chatRoomRepository;
    private final ServicesForAll servicesForAll;

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
                .replyToMessageId(dto.getReplyToMessageId()) // Nullable, for replies
                .serviceCardId(dto.getServiceCardId()) // Nullable, for SERVICE_CARD messageType
                .sentAt(dto.getSentAt())
                .build();

        messageRepository.save(msg);
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                "chat.room." + dto.getChatRoomId(),
                dto
        );
    }

    @Override
    @Transactional
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
                                .replyToMessageId(msg.getReplyToMessageId()) // Nullable, for replies
                                .serviceCardId(msg.getServiceCardId()) // Nullable, for SERVICE_CARD messageType
                                .serviceCard(msg.getServiceCardId() != null
                                        ? servicesForAll.getServiceDto(msg.getServiceCardId()).orElse(null)
                                        : null
                                )
                        .sentAt(msg.getSentAt())
                        .build()
                )
                .toList();
    }

    @Override
    public List<ChatMessageDto> getMessagesBetweenUsers(Long user1Id, Long user2Id) {
        Long loggedIn = authUtils.loggedInUserId();
        if (!loggedIn.equals(user1Id) && !loggedIn.equals(user2Id)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        Long roomId = chatRoomRepository.findDirectRoomBetweenUsers(user1Id, user2Id, ChatRoomType.DIRECT)
                .orElseThrow(() -> new BadRequestException("No direct chat room found between the users"))
                .getRoomId();
        return getMessagesForRoom(roomId);
    }
}
