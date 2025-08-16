package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.TypingStateDto;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.TypingState;
import com.lankatrails.lankatrails_backend.service.TypingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TypingServiceImpl implements TypingService {
    
    private static final long TYPING_TIMEOUT_SECONDS = 5; // 5 seconds timeout
    
    // Map structure: roomId -> (userId -> TypingState)
    private final ConcurrentMap<Long, ConcurrentMap<Long, TypingState>> typingStates = new ConcurrentHashMap<>();
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    public void startTyping(Long roomId, Long userId, String username) {
        log.debug("User {} started typing in room {}", userId, roomId);
        
        typingStates.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(userId, TypingState.builder()
                        .userId(userId)
                        .username(username)
                        .roomId(roomId)
                        .isTyping(true)
                        .lastActivity(Instant.now())
                        .build());
        
        // Broadcast typing state to room
        broadcastTypingState(roomId);
    }
    
    @Override
    public void stopTyping(Long roomId, Long userId) {
        log.debug("User {} stopped typing in room {}", userId, roomId);
        
        ConcurrentMap<Long, TypingState> roomTypingStates = typingStates.get(roomId);
        if (roomTypingStates != null) {
            roomTypingStates.remove(userId);
            if (roomTypingStates.isEmpty()) {
                typingStates.remove(roomId);
            }
        }
        
        // Broadcast updated typing state to room
        broadcastTypingState(roomId);
    }
    
    @Override
    public APIResponse<List<TypingStateDto>> getTypingUsersInRoom(Long roomId) {
        ConcurrentMap<Long, TypingState> roomTypingStates = typingStates.get(roomId);
        if (roomTypingStates == null) {
            return APIResponse.<List<TypingStateDto>>builder()
                    .success(true)
                    .data(List.of())
                    .message("No typing users found in room")
                    .build();
        }

        List<TypingStateDto> typingUsers = roomTypingStates.values().stream()
                .filter(state -> !state.isExpired(TYPING_TIMEOUT_SECONDS))
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.trace("Retrieved {} typing users in room {}", typingUsers.size(), roomId);
        return APIResponse.<List<TypingStateDto>>builder()
                .success(true)
                .data(typingUsers)
                .message("Typing users retrieved successfully")
                .build();
    }
    
    @Override
    @Scheduled(fixedRate = 2000) // Run every 2 seconds
    public void cleanupExpiredTypingStates() {
        log.trace("Running typing state cleanup");
        
        typingStates.entrySet().removeIf(roomEntry -> {
            Long roomId = roomEntry.getKey();
            ConcurrentMap<Long, TypingState> roomStates = roomEntry.getValue();
            
            // Remove expired states
            roomStates.entrySet().removeIf(userEntry -> {
                boolean expired = userEntry.getValue().isExpired(TYPING_TIMEOUT_SECONDS);
                if (expired) {
                    log.debug("Removed expired typing state for user {} in room {}", 
                             userEntry.getKey(), roomId);
                }
                return expired;
            });
            
            // If room has typing updates after cleanup, broadcast the change
            if (!roomStates.isEmpty()) {
                broadcastTypingState(roomId);
            }
            
            // Remove empty room entries
            return roomStates.isEmpty();
        });
    }
    
    private void broadcastTypingState(Long roomId) {
        List<TypingStateDto> typingUsers = getTypingUsersInRoom(roomId).getData();
        
        // Send typing state to RabbitMQ for real-time updates
        try {
            rabbitTemplate.convertAndSend(
                    "chat.exchange",
                    "chat.room." + roomId + ".typing",
                    typingUsers
            );
            log.trace("Broadcasted typing state for room {}: {} users typing", roomId, typingUsers.size());
        } catch (Exception e) {
            log.error("Failed to broadcast typing state for room {}: {}", roomId, e.getMessage());
        }
    }
    
    private TypingStateDto convertToDto(TypingState state) {
        return TypingStateDto.builder()
                .roomId(state.getRoomId())
                .userId(state.getUserId())
                .username(state.getUsername())
                .isTyping(state.isTyping())
                .timestamp(state.getLastActivity())
                .build();
    }
}
