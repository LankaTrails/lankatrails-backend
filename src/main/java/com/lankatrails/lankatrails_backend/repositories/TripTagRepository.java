package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.TripTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripTagRepository extends JpaRepository<TripTag, Long> {

    // Custom query methods can be added here if needed
    // For example, to find a tag by its name:
    // Optional<TripTag> findByTagName(TripTagType tagName);
}
