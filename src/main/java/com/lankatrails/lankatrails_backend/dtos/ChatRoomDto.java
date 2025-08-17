package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDto {
    private Long id;
    private ChatRoomType chatRoomType;
    private List<Long> participantIds;
    private Long tripId; // Optional, can be null if not associated with a trip

//    private LocalDateTime createdAt; // This field is optional, can be set to null if not needed
}
