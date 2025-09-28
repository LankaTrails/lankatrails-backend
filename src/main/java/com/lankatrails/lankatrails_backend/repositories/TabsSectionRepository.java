package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TabsSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabsSectionRepository extends JpaRepository<TabsSection, Long> {
    List<TabsSection> findByService_ServiceId(Long serviceId);
}
