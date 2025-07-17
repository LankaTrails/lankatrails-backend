package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Services;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Services,Long> {
    //Search by distance using PostGIS
//    @Query(value = """
//        SELECT s.*
//        FROM services s
//        JOIN location l ON s.location_id = l.location_id
//        WHERE ST_DWithin(
//            l.coordinates,
//            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
//            :radiusMeters
//        )
//        ORDER BY ST_Distance(
//            l.coordinates,
//            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
//        )
//        """, nativeQuery = true)
//    List<Service> findNearbyServices(
//            @Param("lat") double lat,
//            @Param("lng") double lng,
//            @Param("radiusMeters") double radiusMeters
//    );
//
//    //Text-based location filter
//    @Query("""
//        SELECT s FROM Service s
//        WHERE (:city IS NULL OR LOWER(s.location.city) = LOWER(:city))
//          AND (:district IS NULL OR LOWER(s.location.district) = LOWER(:district))
//          AND (:province IS NULL OR LOWER(s.location.province) = LOWER(:province))
//          AND (:country IS NULL OR LOWER(s.location.country) = LOWER(:country))
//    """)
//    List<Service> findByLocationDetails(
//            @Param("city") String city,
//            @Param("district") String district,
//            @Param("province") String province,
//            @Param("country") String country
//    );
}
