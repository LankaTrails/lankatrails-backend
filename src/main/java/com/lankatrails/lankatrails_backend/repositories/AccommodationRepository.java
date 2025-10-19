package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation,Long> {
    Optional<Accommodation> findByServiceName(String serviceName);
    Optional<Accommodation> findByServiceId(Long id);
    Optional<List<Accommodation>> findByProvider_UserId(Long userId);

}
