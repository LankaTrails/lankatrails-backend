package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripInvitationRepository extends JpaRepository<TripInvitation, Long> {
    Optional<TripInvitation> findByToken(String token);
}
