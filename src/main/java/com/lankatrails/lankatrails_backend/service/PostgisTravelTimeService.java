package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.TransportMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PostgisTravelTimeService {
    @PersistenceContext
    private EntityManager entityManager;

    // Default avg speed per mode
    private static final double DRIVING_SPEED = 50.0;  // km/h
    private static final double WALKING_SPEED = 5.0;   // km/h

    public double calculateDistanceKm(Double fromLat, Double fromLon, Double toLat, Double toLon) {
        String sql = "SELECT ST_DistanceSphere(ST_MakePoint(:lon1, :lat1), ST_MakePoint(:lon2, :lat2)) / 1000";
        Double distance = (Double) entityManager.createNativeQuery(sql)
                .setParameter("lon1", fromLon)
                .setParameter("lat1", fromLat)
                .setParameter("lon2", toLon)
                .setParameter("lat2", toLat)
                .getSingleResult();
        return distance != null ? distance : 0.0;
    }

    public Duration estimateDuration(Double fromLat, Double fromLon, Double toLat, Double toLon, TransportMode mode) {
        double distanceKm = calculateDistanceKm(fromLat, fromLon, toLat, toLon);
        double speed = (mode == TransportMode.DRIVING) ? DRIVING_SPEED : WALKING_SPEED;
        double hours = distanceKm / speed;
        long seconds = (long) (hours * 3600);
        return Duration.ofSeconds(seconds);
    }
}
