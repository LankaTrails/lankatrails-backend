package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.model.Location;
import com.lankatrails.lankatrails_backend.model.enums.TransportMode;

import java.time.Duration;

public interface TravelTimeService {
    Duration calculateTravelTime(Double fromLat, Double fromLon, Double toLat, Double toLon, TransportMode mode);
}

