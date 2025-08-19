package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TouristGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TouristGuideRepository extends JpaRepository<TouristGuide,Long> {
    Optional<TouristGuide> findByServiceName(String serviceName);
    Optional<TouristGuide> findByServiceId(Long Id);
}
