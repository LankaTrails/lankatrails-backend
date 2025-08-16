package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.ChatRoom;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.repositories.ChatRoomRepository;
import com.lankatrails.lankatrails_backend.repositories.TripRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtils authUtils;

    @Override
    @Transactional
    public APIResponse<ChatRoomDto> getDirectChatRoom(Long userId) {
        // Validate users exist and get users
        User user1 = userRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("User with ID " + authUtils.loggedInUserId() + " does not exist"));

        User user2 = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " does not exist"));

        // Check if a direct chat room already exists
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findDirectRoomBetweenUsers(
                user1.getUserId(), user2.getUserId(), ChatRoomType.DIRECT);

        if (existingChatRoom.isPresent()) {
            return APIResponse.<ChatRoomDto>builder()
                    .success(true)
                    .message("Direct chat room already exists")
                    .data(mapToDto(existingChatRoom.get()))
                    .build();
        }

        // Create a new direct chat room
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomType(ChatRoomType.DIRECT);
        chatRoom.setParticipants(List.of(user1, user2));
        chatRoom.setAdmin(null); // Set admin to null for direct chat rooms
        chatRoom.setTrip(null); // Set trip to null for direct chat rooms
        chatRoom.setCreatedAt(java.time.LocalDateTime.now());
        chatRoom = chatRoomRepository.save(chatRoom);

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Direct chat room created successfully")
                .data(mapToDto(chatRoom))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<ChatRoomDto>> getMyChatRooms() {
        Long userId = authUtils.loggedInUserId();
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipants_UserId(userId);

        if (chatRooms.isEmpty()) {
            throw new BadRequestException("No chat rooms found for user with ID " + userId);
        }

        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            chatRoomDtos.add(mapToDto(chatRoom));
        }

        return APIResponse.<List<ChatRoomDto>>builder()
                .success(true)
                .message("Chat rooms retrieved successfully")
                .data(chatRoomDtos)
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<ChatRoomDto> getChatRoomById(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Chat room with ID " + roomId + " does not exist"));

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Chat room retrieved successfully")
                .data(mapToDto(chatRoom))
                .build();
    }

    @Override
    public ChatRoomDto setChatRoomForTrip(Trip trip) {
        // Get the chat room for the trip else set null
        ChatRoom chatRoom = chatRoomRepository.findByTrip_TripId(trip.getTripId())
                .orElse(null);

        if (chatRoom == null) {
            // Create a new chat room if it doesn't exist
            ChatRoom NewChatRoom = new ChatRoom();
            NewChatRoom.setChatRoomType(ChatRoomType.GROUP);
            NewChatRoom.setTrip(trip);
            NewChatRoom.setParticipants(new ArrayList<>(trip.getTourists()));
            NewChatRoom.setAdmin(trip.getLeadTourist());
            NewChatRoom.setCreatedAt(java.time.LocalDateTime.now());
            NewChatRoom = chatRoomRepository.save(NewChatRoom);
            return mapToDto(NewChatRoom);
        } else {
            // Update the chat room participants if it exists
            chatRoom.setParticipants(new ArrayList<>(trip.getTourists()));
            chatRoom = chatRoomRepository.save(chatRoom);
            return mapToDto(chatRoom);
        }
    }

    @Override
    public Boolean isUserInRoom(Long userId, Long chatRoomId) {
        return chatRoomRepository.existsByRoomIdAndParticipants_UserId(chatRoomId, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public ChatRoomDto mapToDto(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }
        ChatRoomDto chatRoomDto = modelMapper.map(chatRoom, ChatRoomDto.class);
        chatRoomDto.setParticipantIds(new ArrayList<>());
        for (User participant : chatRoom.getParticipants()) {
            chatRoomDto.getParticipantIds().add(participant.getUserId());
        }
        if (chatRoom.getTrip() != null) {
            chatRoomDto.setTripId(chatRoom.getTrip().getTripId());
        }
        return chatRoomDto;
    }
}
