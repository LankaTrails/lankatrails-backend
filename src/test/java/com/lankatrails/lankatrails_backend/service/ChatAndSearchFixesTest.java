package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.ReadReceiptEvent;
import com.lankatrails.lankatrails_backend.model.ChatMessage;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.repositories.MessageRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatAndSearchFixesTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ReadReceipts {
        @Mock MessageRepository messageRepository;
        @Mock ChatRoomService chatRoomService;
        @Mock AuthUtils authUtils;
        @Mock SimpMessagingTemplate messagingTemplate;
        @InjectMocks ChatServiceImpl chatService;

        @Test
        void markingAMessageReadBroadcastsAReceiptToTheRoom() {
            ChatMessage message = ChatMessage.builder().id("m1").chatRoomId(5L).senderId(2L).build();
            when(messageRepository.findById("m1")).thenReturn(Optional.of(message));
            when(chatRoomService.isUserInRoom(1L, 5L)).thenReturn(true);

            chatService.markMessageAsRead("m1", 1L);

            ArgumentCaptor<ReadReceiptEvent> event = ArgumentCaptor.forClass(ReadReceiptEvent.class);
            verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/read.5"), event.capture());
            assertThat(event.getValue().getReaderId()).isEqualTo(1L);
            assertThat(event.getValue().getMessageIds()).containsExactly("m1");
            assertThat(message.getReadBy()).containsKey(1L);
        }

        @Test
        void nonMembersCannotMarkMessagesRead() {
            ChatMessage message = ChatMessage.builder().id("m1").chatRoomId(5L).senderId(2L).build();
            when(messageRepository.findById("m1")).thenReturn(Optional.of(message));
            when(chatRoomService.isUserInRoom(9L, 5L)).thenReturn(false);

            assertThatThrownBy(() -> chatService.markMessageAsRead("m1", 9L)).isInstanceOf(AccessDeniedException.class);
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        void nonMembersCannotReadRoomHistory() {
            when(authUtils.loggedInUserId()).thenReturn(9L);
            when(chatRoomService.isUserInRoom(9L, 5L)).thenReturn(false);

            assertThatThrownBy(() -> chatService.getMessagesForRoom(5L)).isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    class NearbySearch {
        private Service service(long id) {
            Service service = new Service();
            service.setServiceId(id);
            return service;
        }

        @Test
        void keepsEachServiceOnceInNearestFirstOrder() {
            Service a = service(1L);
            Service b = service(2L);
            List<Object[]> rows = new ArrayList<>();
            rows.add(new Object[]{b, 120.0}); // b's nearest location
            rows.add(new Object[]{a, 300.0});
            rows.add(new Object[]{b, 900.0}); // b's second location, further away

            assertThat(ServiceRepository.distinctServices(rows)).containsExactly(b, a);
        }

        @Test
        void emptyResultStaysEmpty() {
            assertThat(ServiceRepository.distinctServices(List.of())).isEmpty();
        }
    }
}
