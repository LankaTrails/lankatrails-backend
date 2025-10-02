package com.lankatrails.lankatrails_backend.rabbit;

import com.lankatrails.lankatrails_backend.dtos.TypingStateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class TypingRabbitListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "#{typingQueue.name}")
    public void handleTypingEvent(TypingStateDto typingStateDto) {
        messagingTemplate.convertAndSend(
                "/topic/typing." + typingStateDto.getRoomId(),
                typingStateDto
        );
    }
}