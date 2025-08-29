package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.AvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailableTime,Long> {
    List<AvailableTime> findByService_ServiceId(Long Id);
}
