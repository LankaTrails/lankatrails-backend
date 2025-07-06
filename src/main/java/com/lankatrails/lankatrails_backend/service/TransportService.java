package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;

public interface TransportService {
    TransportResponseDTO getAll(Integer pageNumber, Integer pageSize);
    TransportResponseDTO getById(Long Id);
    TransportResponseDTO updateTransport(Long Id, TransportRequestDTO transportRequestDTO);
    TransportResponseDTO addNewTransport(TransportRequestDTO transportRequestDTO);
    APIResponse<String> deleteTransport(Long Id);


}
