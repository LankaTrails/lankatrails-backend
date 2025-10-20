package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.model.enums.TransportMode;
import com.lankatrails.lankatrails_backend.service.GoogleMapsTravelTimeService;
import com.lankatrails.lankatrails_backend.service.PostgisTravelTimeService;
import com.lankatrails.lankatrails_backend.service.TravelTimeService;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class HybridTravelTimeService implements TravelTimeService {

    private final PostgisTravelTimeService postgisService;
    private final GoogleMapsTravelTimeService googleService;

    // Thresholds (can be moved to application.yml)
    private static final double DRIVING_THRESHOLD_KM = 200.0;
    private static final double WALKING_THRESHOLD_KM = 15.0;

    public HybridTravelTimeService(PostgisTravelTimeService postgisService,
                                   GoogleMapsTravelTimeService googleService) {
        this.postgisService = postgisService;
        this.googleService = googleService;
    }

    @Override
    public Duration calculateTravelTime(Double fromLat, Double fromLon, Double toLat, Double toLon, TransportMode mode) {
        double distanceKm = postgisService.calculateDistanceKm(fromLat, fromLon, toLat, toLon);

        // Quick rejection: absurdly large displacement
        if ((mode == TransportMode.DRIVING && distanceKm > 1000) ||
                (mode == TransportMode.WALKING && distanceKm > 100)) {
            throw new IllegalArgumentException("Locations too far apart");
        }

        // If within threshold → call Google for accuracy
        if ((mode == TransportMode.DRIVING && distanceKm <= DRIVING_THRESHOLD_KM) ||
                (mode == TransportMode.WALKING && distanceKm <= WALKING_THRESHOLD_KM)) {
            try {
                return googleService.getDuration(fromLat, fromLon, toLat, toLon, mode);
            } catch (Exception e) {
                // Fallback to PostGIS if Google fails
                return postgisService.estimateDuration(fromLat, fromLon, toLat, toLon, mode);
            }
        }

        // Otherwise → just use PostGIS approximation
        return postgisService.estimateDuration(fromLat, fromLon, toLat, toLon, mode);
    }

}

