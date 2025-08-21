package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    // 1. Nearby services by distance (unchanged)
    @Query("""
                SELECT DISTINCT s FROM Service s
                JOIN s.locations l
                WHERE function('ST_DWithin',
                    l.coordinates,
                    function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326),
                    :radiusMeters
                ) = true
                ORDER BY function('ST_Distance',
                    l.coordinates,
                    function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326)
                )
            """)
    List<Service> findNearbyServices(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters
    );


    // 2. Fixed search using explicit text casting with ILIKE
//    @Query("""
//                SELECT DISTINCT s FROM Service s
//                JOIN s.locations l
//                WHERE (:city IS NULL OR (l.city) ILIKE (CONCAT('%', :city, '%')))
//                  AND (:district IS NULL OR (l.district) ILIKE (CONCAT('%', :district, '%')))
//                  AND (:province IS NULL OR (l.province) ILIKE (CONCAT('%', :province, '%')))
//                  AND (:country IS NULL OR (l.country) ILIKE (CONCAT('%', :country, '%')))
//            """)
//    List<Service> findByLocationDetails(
//            @Param("city") String city,
//            @Param("district") String district,
//            @Param("province") String province,
//            @Param("country") String country
//    );

    @Query("""
                SELECT DISTINCT s FROM Service s
                JOIN s.locations l
                WHERE (l.country) ILIKE 'sri lanka'
                  AND (
                       :location IS NULL OR
                       (l.city) ILIKE (CONCAT('%', :location, '%')) OR
                       (l.district) ILIKE (CONCAT('%', :location, '%'))
                  )
            """)
    List<Service> findByLocationInSriLanka(@Param("location") String location);


    // 3. Combined spatial + text search with fixed casting
//    @Query("""
//            SELECT s FROM Service s
//            WHERE function('ST_DWithin',
//                s.locationBased.coordinates,
//                function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326),
//                :radiusMeters
//            ) = true
//              AND (:city IS NULL OR s.locationBased.city ILIKE CONCAT('%', CAST(:city AS text), '%'))
//              AND (:district IS NULL OR s.locationBased.district ILIKE CONCAT('%', CAST(:district AS text), '%'))
//              AND (:province IS NULL OR s.locationBased.province ILIKE CONCAT('%', CAST(:province AS text), '%'))
//              AND (:country IS NULL OR s.locationBased.country ILIKE CONCAT('%', CAST(:country AS text), '%'))
//            ORDER BY function('ST_Distance',
//                s.locationBased.coordinates,
//                function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326)
//            )
//            """)
//    List<Service> findNearbyServicesWithLocationFilter(
//            @Param("lat") double lat,
//            @Param("lng") double lng,
//            @Param("radiusMeters") double radiusMeters,
//            @Param("city") String city,
//            @Param("district") String district,
//            @Param("province") String province,
//            @Param("country") String country
//    );

    @Query("""
                SELECT DISTINCT s FROM Service s
                JOIN s.locations l
                WHERE l.country ILIKE 'Sri Lanka'
                  AND (
                      :location IS NULL OR
                      l.city ILIKE CONCAT('%', :location, '%') OR
                      l.district ILIKE CONCAT('%', :location, '%')
                  )
                  AND (:providerId IS NULL OR s.provider.id = :providerId)
                  AND (:categoryId IS NULL OR s.category.id = :categoryId)
            """)
    List<Service> findByLocationProviderCategory(
            @Param("location") String location,
            @Param("providerId") Long providerId,
            @Param("categoryId") Long categoryId
    );


    @Query("""
                SELECT DISTINCT s FROM Service s
                JOIN s.locations l
                WHERE function('ST_DWithin',
                    l.coordinates,
                    function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326),
                    :radiusMeters
                ) = true
                AND (:providerId IS NULL OR s.provider.id = :providerId)
                AND (:categoryId IS NULL OR s.category.id = :categoryId)
                ORDER BY function('ST_Distance',
                    l.coordinates,
                    function('ST_SetSRID', function('ST_MakePoint', :lng, :lat), 4326)
                )
            """)
    List<Service> findNearbyServicesByProviderCategory(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusMeters") double radiusMeters,
            @Param("providerId") Long providerId,
            @Param("categoryId") Long categoryId
    );


    List<Service> findByProviderAndCategory(Provider provider, Category category);
    List<Service> findByCategoryAndProvider(Category category, Provider provider);


}