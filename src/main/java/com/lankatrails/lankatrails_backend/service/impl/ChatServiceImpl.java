package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.exception.UnauthorizedException;
import com.lankatrails.lankatrails_backend.model.enums.ChatMessageType;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Validate message type and content
        if (dto.getMessageType() == ChatMessageType.SERVICE_CARD && dto.getServiceCardId() == null) {
            throw new BadRequestException("Service card ID must be provided for SERVICE_CARD message type");
        } else if (dto.getMessageType() == ChatMessageType.TEXT && (dto.getContent() == null || dto.getContent().isEmpty())) {
            throw new BadRequestException("Content must be provided for TEXT message type");
        } else if (dto.getMessageType() == ChatMessageType.REPLY && dto.getReplyToMessageId() == null) {
            throw new BadRequestException("Reply message ID must be provided for REPLY message type");
        }

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

        List<ChatMessage> messages = messageRepository.findByChatRoomIdOrderBySentAtAsc(roomId);

        // Batch fetch service cards
        Map<Long, ServiceDTO> serviceCards = fetchServiceCards(messages);

        return messages.stream()
                .map(msg -> convertToDto(msg, serviceCards))
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

    private Map<Long, ServiceDTO> fetchServiceCards(List<ChatMessage> messages) {
        Set<Long> serviceIds = messages.stream()
                .filter(msg -> msg.getMessageType() == ChatMessageType.SERVICE_CARD)
                .map(ChatMessage::getServiceCardId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return servicesForAll.getServiceDtos(serviceIds); // Implement batch method
    }

    private ChatMessageDto convertToDto(ChatMessage msg, Map<Long, ServiceDTO> serviceCards) {
        return ChatMessageDto.builder()
                .chatRoomId(msg.getChatRoomId())
                .senderId(msg.getSenderId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .replyToMessageId(msg.getReplyToMessageId())
                .serviceCardId(msg.getServiceCardId())
                .serviceCard(serviceCards.get(msg.getServiceCardId()))
                .sentAt(msg.getSentAt())
                .build();
    }
}
