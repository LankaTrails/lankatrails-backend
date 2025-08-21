package com.lankatrails.lankatrails_backend.repositories;
import com.lankatrails.lankatrails_backend.model.RateAndReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<RateAndReview, Long> {
//    List<RateAndReview> findByServiceId(Long serviceId);
//    List<RateAndReview> findByTouristId(Long touristId);
//    Optional<RateAndReview> findByIdAndTouristId(Long id, Long touristId);
    boolean existsByService_ServiceIdAndTourist_UserId(Long serviceId, Long touristId);
}
