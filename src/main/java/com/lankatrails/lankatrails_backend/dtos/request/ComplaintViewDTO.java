package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.response.ApproveLicenseResponse;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintViewDTO {
    private String touristEmail;
    private String description;
    private BusinessType businessType;
    private UserStatus userStatus;
    private String serviceName;
    private Integer totalComplaints;
    private CategoryRequestDTO category;
    private List<BookingRequestDTO> bookings;
    private Long complaintId;
    private Long bookingId;
    private LocalDateTime complaintDateTime;
    private BookingRequestDTO booking;
    private String investigationStartedDate;
    private ComplaintResult complaintResult;
    private List<String> complaintImgs;
    private String paidAmount;
    private RefundStatus refundStatus;
    private String refundReason;
    private ComplaintStatus complaintStatus;
    private String adminToTourist;
    private String adminToProvider;

}
