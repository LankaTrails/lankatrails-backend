package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripItemRepository extends JpaRepository<TripItem, Long> {
    // Additional query methods can be defined here if needed
}
