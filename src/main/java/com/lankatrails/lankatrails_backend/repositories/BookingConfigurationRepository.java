package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.BookingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingConfigurationRepository extends JpaRepository<BookingConfiguration, Long> {
}
