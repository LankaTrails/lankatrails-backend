package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatRoomDto extends ChatRoomDto {
    private Long tripId;
    private List<TouristDto> participants;
}
