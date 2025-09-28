package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Method to find an admin by user ID
    Optional<Admin> findByUserId(Long userId);

    // Method to find an admin by email
    Optional<Admin> findByEmail(String email);


}
