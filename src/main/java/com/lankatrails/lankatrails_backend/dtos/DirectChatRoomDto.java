package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.model.enums.ChatRoomType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectChatRoomDto extends ChatRoomDto {
    private Long providerId;
    private Long touristId;
}
