package com.lankatrails.lankatrails_backend.repositories;
import com.lankatrails.lankatrails_backend.model.RateAndReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<RateAndReview, Long> {
    List<RateAndReview> findByService_ServiceId(Long serviceId);
    @Query("SELECT AVG(r.rate) FROM RateAndReview r WHERE r.service.serviceId = :serviceId")
    Double findAverageRatingByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(r) FROM RateAndReview r WHERE r.service.serviceId = :serviceId")
    Long countReviewsByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT AVG(r.rate) FROM RateAndReview r")
    Double findGlobalAverageRating();

}
