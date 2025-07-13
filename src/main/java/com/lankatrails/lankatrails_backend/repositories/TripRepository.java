package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
    // Additional query methods can be defined here if needed
}
