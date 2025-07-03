package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;

public interface TransportService {
    TransportResponseDTO getAll(Integer pageNumber, Integer pageSize);
}
