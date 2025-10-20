package com.lankatrails.lankatrails_backend.dtos.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.lankatrails.lankatrails_backend.model.enums.BookingStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBookingCreateRequest {
    
    private Long tripItemId; // Optional - can be null
    
    @NotNull(message = "Service ID is required")
    private Long serviceId;
    
    @NotNull(message = "Start date time is required")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "End date time is required")
    private LocalDateTime endDateTime;
    
    @NotNull(message = "Total price is required")
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;
    
    private BigDecimal paidAmount;
    
    private BigDecimal depositAmount;
    
    @NotNull(message = "Booking status is required")
    private BookingStatus bookingStatus;
}