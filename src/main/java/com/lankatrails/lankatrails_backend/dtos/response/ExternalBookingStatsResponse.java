package com.lankatrails.lankatrails_backend.dtos.response;

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
public class ExternalBookingStatsResponse {
    
    private Long totalBookings;
    
    private Long confirmedBookings;
    
    private Long pendingBookings;
    
    private Long cancelledBookings;
    
    private Long recentBookings;
}