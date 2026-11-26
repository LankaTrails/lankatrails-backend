package com.lankatrails.lankatrails_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/** Broadcast on /topic/read.{roomId} when a participant reads messages. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptEvent {
    private Long roomId;
    private Long readerId;
    private List<String> messageIds;
    private Instant readAt;
}
