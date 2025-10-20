package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalBookingResponse {
    
    private Long bookingId;
    
    private Long tripItemId;
    
    private Long serviceId;
    
    private String serviceName;
    
    private LocalDateTime bookedDateTime;
    
    private LocalDateTime startDateTime;
    
    private LocalDateTime endDateTime;
    
    private BigDecimal totalPrice;
    
    private BigDecimal paidAmount;
    
    private BigDecimal depositAmount;
    
    private BigDecimal remainingAmount;
    
    private BookingStatus bookingStatus;
}