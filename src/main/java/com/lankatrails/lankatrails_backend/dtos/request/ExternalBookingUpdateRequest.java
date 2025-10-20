package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBookingUpdateRequest {
    
    private LocalDateTime startDateTime;
    
    private LocalDateTime endDateTime;
    
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;
    
    private BigDecimal paidAmount;
    
    private BigDecimal depositAmount;
    
    private BookingStatus bookingStatus;
}