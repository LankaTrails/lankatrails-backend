package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.GuidingArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceAreaRepository extends JpaRepository<GuidingArea,Long> {
    Optional<GuidingArea> findByTouristGuide_ServiceId(Long ServiceId);
}
