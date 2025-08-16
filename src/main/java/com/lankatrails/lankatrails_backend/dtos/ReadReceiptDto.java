package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadReceiptDto {
    private String messageId;
    private Long roomId; // for marking all messages as read in a room
}
