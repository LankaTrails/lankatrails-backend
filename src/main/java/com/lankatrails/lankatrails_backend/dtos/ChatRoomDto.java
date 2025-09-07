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
    private LocalDateTime createdAt;
}
