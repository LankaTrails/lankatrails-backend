package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransportRepository extends JpaRepository<Transport,Long> {
    Optional<Transport> findByServiceName(String serviceName);
}
