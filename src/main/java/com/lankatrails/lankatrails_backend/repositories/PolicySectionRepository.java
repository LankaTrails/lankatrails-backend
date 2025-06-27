package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.PolicySection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicySectionRepository extends JpaRepository<PolicySection,Long> {
    List<PolicySection> findByService_ServiceId(Long Id);
}
