package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.dtos.request.TabSectionRequest;
import com.lankatrails.lankatrails_backend.model.TabsSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TabsSectionRepository extends JpaRepository<TabsSection,Long> {
    List<TabsSection> findByService_ServiceId(Long serviceId);
}
