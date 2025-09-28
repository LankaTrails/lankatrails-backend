package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.ChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.DirectChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.GroupChatRoomDto;
import com.lankatrails.lankatrails_backend.dtos.TouristDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.exception.BadRequestException;
import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.*;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.ChatRoomService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    DirectChatRoomRepository directChatRoomRepository;

    @Autowired
    GroupChatRoomRepository groupChatRoomRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtils authUtils;

    @Override
    @Transactional
    public APIResponse<DirectChatRoomDto> getDirectChatRoom(Long providerId) {
        // Validate users exist and get users
        Tourist tourist = touristRepository.findById(authUtils.loggedInUserId())
                .orElseThrow(() -> new UserNotFoundException("Tourist with ID " + authUtils.loggedInUserId() + " does not exist"));

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + providerId + " does not exist"));

        // Check if a direct chat room already exists
        Optional<DirectChatRoom> existingChatRoom = directChatRoomRepository.findByProvider_UserIdAndTourist_UserId(providerId, tourist.getUserId());

        if (existingChatRoom.isPresent()) {
            return APIResponse.<DirectChatRoomDto>builder()
                    .success(true)
                    .message("Direct chat room already exists")
                    .data(modelMapper.map(existingChatRoom.get(), DirectChatRoomDto.class))
                    .build();
        }

        // Create a new direct chat room
        DirectChatRoom newChatRoom = new DirectChatRoom();
        newChatRoom.setChatRoomType(ChatRoomType.DIRECT);
        newChatRoom.setProvider(provider);
        newChatRoom.setTourist(tourist);
        newChatRoom.setCreatedAt(LocalDateTime.now());
        newChatRoom = directChatRoomRepository.save(newChatRoom);

        DirectChatRoomDto chatRoomDto = modelMapper.map(newChatRoom, DirectChatRoomDto.class);

        // Return the response
        return APIResponse.<DirectChatRoomDto>builder()
                .success(true)
                .message("Direct chat room created successfully")
                .data(chatRoomDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<List<DirectChatRoomDto>> getMyDirectChatRooms() {
        Long userId = authUtils.loggedInUserId();
        UserRole userRole = authUtils.loggedInUserRole();

        Set<DirectChatRoom> chatRooms = new HashSet<>();

        if (userRole == UserRole.ROLE_TOURIST) {
            chatRooms = directChatRoomRepository.findByTourist_UserId(userId);
        } else if (userRole == UserRole.ROLE_PROVIDER) {
            chatRooms = directChatRoomRepository.findByProvider_UserId(userId);
        } else {
            throw new BadRequestException("User role is not valid for direct chat rooms");
        }

        return APIResponse.<List<DirectChatRoomDto>>builder()
                .success(true)
                .message("Chat rooms retrieved successfully")
                .data(chatRooms.stream()
                        .map(chatRoom -> modelMapper.map(chatRoom, DirectChatRoomDto.class))
                        .toList())
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
                .data(modelMapper.map(chatRoom, ChatRoomDto.class))
                .build();
    }

    @Override
    @Transactional
    public GroupChatRoomDto setChatRoomForTrip(Trip trip) {
        // Get the chat room for the trip else set null
        GroupChatRoom chatRoom = groupChatRoomRepository.findByTrip_TripId(trip.getTripId());

        if (chatRoom == null) {
            // Create a new chat room if it does not exist
            chatRoom = new GroupChatRoom();
            chatRoom.setChatRoomType(ChatRoomType.GROUP);
            chatRoom.setTrip(trip);
            chatRoom.setCreatedAt(LocalDateTime.now());
            // Set the participants to the tourists of the trip
            chatRoom.setParticipants(new ArrayList<>(trip.getParticipants()));
            chatRoom = groupChatRoomRepository.save(chatRoom);
            return modelMapper.map(chatRoom, GroupChatRoomDto.class);
        } else {
            // Update the chat room participants if it exists
            chatRoom.setParticipants(new ArrayList<>(trip.getParticipants()));
            chatRoom = groupChatRoomRepository.save(chatRoom);
            return modelMapper.map(chatRoom, GroupChatRoomDto.class);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public APIResponse<GroupChatRoomDto> getGroupChatRoomByTripId(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BadRequestException("Trip with ID " + tripId + " does not exist"));

        GroupChatRoom chatRoom = groupChatRoomRepository.findByTrip_TripId(trip.getTripId());

        if (chatRoom == null) {
            throw new BadRequestException("Group chat room for trip with ID " + tripId + " does not exist");
        }

        GroupChatRoomDto chatRoomDto = modelMapper.map(chatRoom, GroupChatRoomDto.class);
        chatRoomDto.setParticipants(chatRoom.getParticipants().stream()
                .map(participant -> modelMapper.map(participant.getTourist(), TouristDto.class))
                .collect(Collectors.toList()));

        return APIResponse.<GroupChatRoomDto>builder()
                .success(true)
                .message("Group chat room retrieved successfully")
                .data(chatRoomDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isUserInRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BadRequestException("Chat room with ID " + chatRoomId + " does not exist"));

        if (chatRoom instanceof DirectChatRoom directChatRoom) {
            return directChatRoom.getProvider().getUserId().equals(userId) ||
                    directChatRoom.getTourist().getUserId().equals(userId);
        } else if (chatRoom instanceof GroupChatRoom groupChatRoom) {
            return groupChatRoom.getParticipants().stream()
                    .anyMatch(participant -> participant.getTourist().getUserId().equals(userId));
        } else {
            throw new BadRequestException("Invalid chat room type");
        }
    }
}
