package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot,Long> {
    List<AvailabilitySlot> findByService_ServiceId(Long Id);
}
