package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.dtos.response.ApproveLicenseResponse;
import com.lankatrails.lankatrails_backend.model.Booking;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.enums.BusinessType;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Category category;
    private List<Booking> bookings;

}
