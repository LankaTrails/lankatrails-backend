package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadReceiptDto {
    private String messageId;
    private Long roomId; // for marking all messages as read in a room
}
