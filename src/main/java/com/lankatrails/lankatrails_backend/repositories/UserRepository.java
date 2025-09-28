package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Changed from userName to email

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole userRole);
}