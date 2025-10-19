package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Accommodation;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TouristGuideRepository extends JpaRepository<TouristGuide,Long> {
    Optional<TouristGuide> findByServiceName(String serviceName);
    Optional<TouristGuide> findByServiceId(Long Id);
    Optional<List<TouristGuide>> findByProvider_UserId(Long userId);

}
