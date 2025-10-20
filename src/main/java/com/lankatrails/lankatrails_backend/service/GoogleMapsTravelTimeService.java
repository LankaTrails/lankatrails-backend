package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.DirectionsResponse;
import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.TransportMode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;

@Service
public class GoogleMapsTravelTimeService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Duration getDuration(Double fromLat, Double fromLon, Double toLat, Double toLon, TransportMode mode) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=%s&key=%s",
                fromLat, fromLon, toLat, toLon, mode.name().toLowerCase(), apiKey
        );

        DirectionsResponse response = restTemplate.getForObject(url, DirectionsResponse.class);

        if (response == null || !"OK".equals(response.getStatus())) {
            throw new RuntimeException("Google Maps API error: " +
                    (response != null ? response.getStatus() : "No response"));
        }

        long seconds = response.getRoutes()
                .getFirst()
                .getLegs()
                .getFirst()
                .getDuration()
                .getValue();

        return Duration.ofSeconds(seconds);
    }
}
