package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintDTO {
    private String description;
    private ComplaintStatus complaintStatus;
    private Long serviceId;
    private Long BookingId;
    private List<ComplaintImgDTO> complaintImgs;
}
