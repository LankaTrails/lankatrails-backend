package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripParticipantRepository extends JpaRepository<TripParticipant, Long> {
    Optional<TripParticipant> findByTourist_UserId(Long userId);

}
