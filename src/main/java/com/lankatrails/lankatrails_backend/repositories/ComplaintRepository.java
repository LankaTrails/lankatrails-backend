package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Complaint;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    List<Complaint> findByComplaintStatusOrComplaintStatusOrComplaintStatus(ComplaintStatus complaintStatus1,ComplaintStatus complaintStatus2,ComplaintStatus complaintStatus3);
    List<Complaint> findByService_ServiceId(Long Id);
}
