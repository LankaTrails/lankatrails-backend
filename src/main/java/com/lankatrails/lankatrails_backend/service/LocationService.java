package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.LocationDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;

import java.util.List;

public interface LocationService {
    APIResponse<List<LocationDTO>> getAllCities();
}
