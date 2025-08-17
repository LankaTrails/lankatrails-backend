package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.TypingStateDto;
import com.lankatrails.lankatrails_backend.service.TypingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class TypingServiceImpl implements TypingService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    public void startTyping(Long roomId, Long userId, String username) {
        log.debug("User {} started typing in room {}", userId, roomId);

        // Create the typing state dto
        TypingStateDto typingStateDto = TypingStateDto.builder()
                .roomId(roomId)
                .userId(userId)
                .username(username)
                .isTyping(true)
                .timestamp(Instant.now())
                .build();

        // publish to RabbitMQ
        rabbitTemplate.convertAndSend(
                "chat.exchange",
                "chat.typing." + roomId,
                typingStateDto
        );
    }
    
    @Override
    public void stopTyping(Long roomId, Long userId, String username) {
        log.debug("User {} stopped typing in room {}", userId, roomId);

        // Create the typing state dto
        TypingStateDto typingStateDto = TypingStateDto.builder()
                .roomId(roomId)
                .userId(userId)
                .username(username)
                .isTyping(false)
                .timestamp(Instant.now())
                .build();

        // publish to RabbitMQ
        rabbitTemplate.convertAndSend(
                "chat.exchange",
                "chat.typing." + roomId,
                typingStateDto
        );
    }
}
