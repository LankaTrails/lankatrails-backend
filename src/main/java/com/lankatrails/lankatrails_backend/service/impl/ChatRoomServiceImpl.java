package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import com.lankatrails.lankatrails_backend.repositories.ChatRoomRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public APIResponse<ChatRoomDto> createChatRoom(ChatRoomDto chatRoomDto) {
        // Get existing chat room if it exists
        Optional<ChatRoom> existingChatRoom = findExistingChatRoom(chatRoomDto);

        if (existingChatRoom.isPresent()) {
            return APIResponse.<ChatRoomDto>builder()
                    .success(false)
                    .message("Chat room already exists")
                    .data(modelMapper.map(existingChatRoom.get(), ChatRoomDto.class))
                    .build();
        }

        // Create new chat room based on type
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomType(chatRoomDto.getChatRoomType());
        chatRoom.setCreatedAt(java.time.LocalDateTime.now());

        List<User> participants = new ArrayList<>();
        for (Long userId : chatRoomDto.getParticipantIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " does not exist"));
            participants.add(user);
        }
        chatRoom.setParticipants(participants);

        // Save the new chat room
        chatRoom = chatRoomRepository.save(chatRoom);

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Chat room created successfully")
                .data(modelMapper.map(chatRoom, ChatRoomDto.class))
                .build();

    }

    public APIResponse<ChatRoomDto> getChatRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Chat room with ID " + roomId + " does not exist"));

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Chat room retrieved successfully")
                .data(modelMapper.map(chatRoom, ChatRoomDto.class))
                .build();
    }

    @Override
    public APIResponse<ChatRoomDto> getChatRoomByTypeAndParticipants(ChatRoomDto chatRoomDto) {
        // Validate the chat room type and participants
        if (chatRoomDto.getChatRoomType() == null || chatRoomDto.getParticipantIds() == null || chatRoomDto.getParticipantIds().isEmpty()) {
            throw new BadRequestException("Chat room type and participants must be provided");
        }

        // Find existing chat room
        Optional<ChatRoom> existingChatRoom = findExistingChatRoom(chatRoomDto);

        if (existingChatRoom.isPresent()) {
            return APIResponse.<ChatRoomDto>builder()
                    .success(true)
                    .message("Chat room found")
                    .data(modelMapper.map(existingChatRoom.get(), ChatRoomDto.class))
                    .build();
        } else {
            return APIResponse.<ChatRoomDto>builder()
                    .success(false)
                    .message("No chat room found for the given type and participants")
                    .data(null)
                    .build();
        }
    }

    @Override
    public Boolean isUserInRoom(Long userId, Long chatRoomId) {
//        // Check if the user exists
//        if (!userRepository.existsById(userId)) {
//            throw new UserNotFoundException("User with ID " + userId + " does not exist");
//        }
//
//        // Check if the chat room exists
//        if (!chatRoomRepository.existsById(chatRoomId)) {
//            throw new BadRequestException("Chat room with ID " + chatRoomId + " does not exist");
//        }

        // Check if the user is a participant in the chat room
        return chatRoomRepository.existsByRoomIdAndParticipants_UserId(chatRoomId, userId);
    }

    private Optional<ChatRoom> findExistingChatRoom(ChatRoomDto chatRoomDto) {
        // Check if users exist
        for (Long userId : chatRoomDto.getParticipantIds()) {
            if (!userRepository.existsById(userId)) {
                throw  new UserNotFoundException("User with ID " + userId + " does not exist");
            }
        }

        switch (chatRoomDto.getChatRoomType()) {
            case DIRECT:
                if (chatRoomDto.getParticipantIds().size() != 2) {
                    throw new BadRequestException("Direct chat rooms must have exactly 2 participants");
                }
                return chatRoomRepository.findDirectRoomBetweenUsers(
                        chatRoomDto.getParticipantIds().get(0),
                        chatRoomDto.getParticipantIds().get(1),
                        chatRoomDto.getChatRoomType()
                );

            case GROUP:
                if (chatRoomDto.getParticipantIds().size() < 2) {
                    throw new BadRequestException("Group chat rooms must have at least 2 participants");
                }
                // For group chat, we can return an empty Optional as we don't check for existing groups here
                return Optional.empty();
            default:
                throw new BadRequestException("Invalid chat room type");

        }
    }
}
