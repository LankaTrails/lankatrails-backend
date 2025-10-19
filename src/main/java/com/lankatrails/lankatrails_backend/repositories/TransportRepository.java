package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ActivityService;
import com.lankatrails.lankatrails_backend.model.TouristGuide;
import com.lankatrails.lankatrails_backend.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportRepository extends JpaRepository<Transport,Long> {
    Optional<Transport> findByServiceName(String serviceName);
    Optional<List<Transport>> findByProvider_UserId(Long userId);

}
