package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRoomDto extends ChatRoomDto {
    private Long tripId;
    private Long[] participantIds;
}
