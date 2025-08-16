package com.lankatrails.lankatrails_backend.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lankatrails.lankatrails_backend.model.Trip;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
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

    @Override
    @Transactional
    public APIResponse<ChatRoomDto> getChatRoom(ChatRoomDto chatRoomDto) {
        // Get existing chat room if it exists
        Optional<ChatRoom> existingChatRoom = findExistingChatRoom(chatRoomDto);

        if (existingChatRoom.isPresent()) {
            return APIResponse.<ChatRoomDto>builder()
                    .success(true)
                    .message("Existing chat room found")
                    .data(mapToDto(existingChatRoom.get()))
                    .build();
        }

        // Create new chat room based on type
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomType(chatRoomDto.getChatRoomType());
        if (chatRoomDto.getTripId() != null && chatRoomDto.getChatRoomType() == ChatRoomType.GROUP) {
            Trip trip = tripRepository.findByTripId(chatRoomDto.getTripId())
                    .orElseThrow(() -> new BadRequestException("Trip with ID " + chatRoomDto.getTripId() + " does not exist"));
            chatRoom.setTrip(trip);
            chatRoom.setParticipants(new ArrayList<>(trip.getTourists()));
            chatRoom.setAdmin(chatRoom.getTrip().getLeadTourist());
        } else if (chatRoomDto.getChatRoomType() == ChatRoomType.DIRECT) {
            List<User> participants = new ArrayList<>();
            for (Long userId : chatRoomDto.getParticipantIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " does not exist"));
                participants.add(user);
            }
            chatRoom.setParticipants(participants);
            chatRoom.setAdmin(null);
        } else {
            throw new BadRequestException("Invalid chat room type or missing trip ID for group chat");
        }
        chatRoom.setCreatedAt(java.time.LocalDateTime.now());

        // Save the new chat room
        chatRoom = chatRoomRepository.save(chatRoom);

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Chat room created successfully")
                .data(modelMapper.map(chatRoom, ChatRoomDto.class))
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
    @Transactional(readOnly = true)
    public APIResponse<ChatRoomDto> getChatRoomByTypeAndParticipants(ChatRoomDto chatRoomDto) {
        // Validate the chat room type and participants
        if (chatRoomDto.getChatRoomType() == null || chatRoomDto.getParticipantIds() == null
                || chatRoomDto.getParticipantIds().isEmpty()) {
            throw new BadRequestException("Chat room type and participants must be provided");
        }

        // Find existing chat room
        Optional<ChatRoom> existingChatRoom = findExistingChatRoom(chatRoomDto);

        if (existingChatRoom.isPresent()) {
            return APIResponse.<ChatRoomDto>builder()
                    .success(true)
                    .message("Chat room found")
                    .data(mapToDto(existingChatRoom.get()))
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
    @Transactional(readOnly = true)
    public APIResponse<ChatRoomDto> getChatRoomByTripId(Long tripId) {
        // Check if the trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new BadRequestException("Trip with ID " + tripId + " does not exist");
        }

        // Find chat room by trip ID
        ChatRoom chatRoom = chatRoomRepository.findByTrip_TripId(tripId)
                .orElseThrow(() -> new BadRequestException("No chat room found for trip ID " + tripId));

        return APIResponse.<ChatRoomDto>builder()
                .success(true)
                .message("Chat room retrieved successfully")
                .data(mapToDto(chatRoom))
                .build();
    }

    @Override
    public void setChatRoomForTrip(Trip trip) {
        // Get the chat room for the trip else set null
        ChatRoom chatRoom = chatRoomRepository.findByTrip_TripId(trip.getTripId())
                .orElse(null);

        if (chatRoom == null) {
            // Create a new chat room if it doesn't exist
            chatRoom = createChatRoomForTrip(trip);
        } else {
            // Update the chat room participants if it exists
            chatRoom.setParticipants(new ArrayList<>(trip.getTourists()));
            chatRoom = chatRoomRepository.save(chatRoom);
        }
    }

    private ChatRoom createChatRoomForTrip(Trip trip) {
        // Create a new chat room for the trip
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomType(ChatRoomType.GROUP);
        chatRoom.setTrip(trip);
        chatRoom.setParticipants(new ArrayList<>(trip.getTourists()));
        chatRoom.setAdmin(trip.getLeadTourist());
        chatRoom.setCreatedAt(java.time.LocalDateTime.now());

        // Save the new chat room
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public Boolean isUserInRoom(Long userId, Long chatRoomId) {
        return chatRoomRepository.existsByRoomIdAndParticipants_UserId(chatRoomId, userId);
    }

    @Transactional
    private Optional<ChatRoom> findExistingChatRoom(ChatRoomDto chatRoomDto) {
        // Check if users exist
        for (Long userId : chatRoomDto.getParticipantIds()) {
            if (!userRepository.existsById(userId)) {
                throw new UserNotFoundException("User with ID " + userId + " does not exist");
            }
        }

        return switch (chatRoomDto.getChatRoomType()) {
            case DIRECT -> {
                if (chatRoomDto.getParticipantIds().size() != 2) {
                    throw new BadRequestException("Direct chat rooms must have exactly 2 participants");
                }
                yield chatRoomRepository.findDirectRoomBetweenUsers(
                        chatRoomDto.getParticipantIds().get(0),
                        chatRoomDto.getParticipantIds().get(1),
                        chatRoomDto.getChatRoomType());
            }
            case GROUP -> {
                if (chatRoomDto.getParticipantIds().size() < 2) {
                    throw new BadRequestException("Group chat rooms must have at least 2 participants");
                }
                yield chatRoomRepository.findByTrip_TripId(chatRoomDto.getTripId());
            }
            default -> throw new BadRequestException("Invalid chat room type");
        };
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
