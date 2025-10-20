package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Complaint;
import com.lankatrails.lankatrails_backend.model.ComplaintImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintImgRepository extends JpaRepository<ComplaintImage,Long> {
    List<ComplaintImage> findByComplaint_ComplaintId(Long aLong);
    List<ComplaintImage> findByComplaint(Complaint complaint);
}
