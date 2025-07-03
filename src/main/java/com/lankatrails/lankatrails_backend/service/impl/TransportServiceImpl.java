package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.ActivityServiceResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.exception.APIException;
import com.lankatrails.lankatrails_backend.model.Transport;
import com.lankatrails.lankatrails_backend.repositories.TransportRepository;
import com.lankatrails.lankatrails_backend.service.TransportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransportServiceImpl implements TransportService {

    @Autowired
    TransportRepository transportRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public TransportResponseDTO getAll(Integer pageNumber, Integer pageSize){
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize);
        Page<Transport> transportPage=transportRepository.findAll(pageDetails);
        List<Transport> transports=transportPage.getContent();

        if (transports.isEmpty()){
            throw new APIException("No Transport Created Until Now");
        }

        List<TransportRequestDTO>  transport_DTOs=transports.stream()
                .map(transport -> modelMapper.map(transport, TransportRequestDTO.class))
                .toList();

        TransportResponseDTO transportResponseDTO=new TransportResponseDTO();

        transportResponseDTO.setContent(transport_DTOs);
        transportResponseDTO.setLastPage(transportPage.isLast());
        transportResponseDTO.setPageNumber(transportPage.getNumber());
        transportResponseDTO.setPageSize(transportPage.getSize());
        transportResponseDTO.setTotalElements(transportPage.getTotalElements());
        transportResponseDTO.setTotalPages(transportPage.getTotalPages());

        return transportResponseDTO;

    }
}
