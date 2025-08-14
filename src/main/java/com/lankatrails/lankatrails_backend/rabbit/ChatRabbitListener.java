package com.lankatrails.lankatrails_backend.rabbit;

import com.lankatrails.lankatrails_backend.dtos.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRabbitListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "#{instanceQueue.name}")
    public void handleChatMessage(ChatMessageDto message) {
        messagingTemplate.convertAndSend("/topic/room." + message.getChatRoomId(), message);
    }
}
