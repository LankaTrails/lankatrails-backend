package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TouristGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TouristGuideRepository extends JpaRepository<TouristGuide,Long> {
    Optional<TouristGuide> findByServiceName(String serviceName);
    Optional<TouristGuide> findByServiceId(Long Id);
}
