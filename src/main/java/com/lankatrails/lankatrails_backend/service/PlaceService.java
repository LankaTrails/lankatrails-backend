package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.PlaceDTO;
import com.lankatrails.lankatrails_backend.model.Place;

public interface PlaceService {
    Place createPlace(PlaceDTO place);

    Place updatePlace(PlaceDTO place);
}
