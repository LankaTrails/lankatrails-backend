package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.PriceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceConfigurationRepository extends JpaRepository<PriceConfiguration, Long> {
}
