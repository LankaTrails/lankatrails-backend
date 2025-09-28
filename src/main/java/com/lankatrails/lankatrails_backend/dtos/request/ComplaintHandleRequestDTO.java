package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.enums.ComplaintResult;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintHandleRequestDTO {
    private Long serviceId;
    private Long userId; //tourist
    private Long complaintId;
    private Long resolveId;
    private Long rejectId;
    private ComplaintStatus complaintStatus;
    private ComplaintResult complaintResult;
    private ComplaintType complaintType; //who's fault
    private Boolean warningStatus;
    private String warningReason;


}
