package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Custom query methods can be defined here if needed
    Optional<Location> findLocationByLocationId(Long id);

    @Query("SELECT l FROM Location l WHERE l.city IS NOT NULL GROUP BY l.city, l.locationId")
    List<Location> findFirstLocationPerCity();

    List<Location> findByLocationType(LocationType locationType);

}
