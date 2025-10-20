package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License,Long> {
    List<License> findByProvider_UserIdAndCategory_CategoryId(Long providerId, Integer categoryId);
    List<License> findByProvider_UserId(Long userId);
}
