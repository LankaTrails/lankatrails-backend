package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TouristRepository extends JpaRepository<Tourist, Long> {
    Optional<Tourist> findByUserId(Long userId);
    // Additional query methods can be defined here if needed
}
