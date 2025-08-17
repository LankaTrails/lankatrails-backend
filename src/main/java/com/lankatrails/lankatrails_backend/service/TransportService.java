package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.model.PolicySection;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TransportService {
    APIResponse<TransportResponseDTO> getAll(Integer pageNumber, Integer pageSize);
    APIResponse<TransportRequestDTO> getById(Long Id);
    TransportResponseDTO updateTransport(Long Id, TransportRequestDTO transportRequestDTO);
    APIResponse<String> addNewTransport(TransportRequestDTO transportRequestDTO, List<MultipartFile> images);
    APIResponse<String> deleteTransport(Long Id);
    APIResponse<String> addNewPolicy(PolicySection policies);
    APIResponse<String> updateTransport(Long id, TransportRequestDTO transportRequestDTO, List<MultipartFile> images);


}
