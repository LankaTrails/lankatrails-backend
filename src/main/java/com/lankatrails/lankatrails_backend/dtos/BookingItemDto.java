package com.lankatrails.lankatrails_backend.dtos;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItemDto {
    private Long tripItemId;
    private ServiceDTO service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer noOfUnits;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private BookingStatus status;
    private BigDecimal totalPrice;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private BigDecimal depositAmount;
    private LocalDateTime bookingDate;
}
