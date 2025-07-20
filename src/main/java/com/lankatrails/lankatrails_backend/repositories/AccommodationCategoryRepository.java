package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.AccommodationCategory;
import com.lankatrails.lankatrails_backend.model.enums.AccommodationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccommodationCategoryRepository extends JpaRepository<AccommodationCategory, Long> {
    Optional<AccommodationCategory> findByCategoryName(AccommodationType categoryName);


}
