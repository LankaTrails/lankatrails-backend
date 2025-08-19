package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Method to find an admin by user ID
    Optional<Admin> findByUserId(Long userId);

    // Method to find an admin by email
    Optional<Admin> findByEmail(String email);


}
