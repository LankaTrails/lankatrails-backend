package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripInvitationRepository extends JpaRepository<TripInvitation, Long> {
    Optional<TripInvitation> findByToken(String token);
}
