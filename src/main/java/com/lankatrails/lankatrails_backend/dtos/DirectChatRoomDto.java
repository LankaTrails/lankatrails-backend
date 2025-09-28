package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DirectChatRoomDto extends ChatRoomDto {
    private Long providerId;
    private Long touristId;
    private ProviderDto provider;
    private TouristDto tourist;
}
