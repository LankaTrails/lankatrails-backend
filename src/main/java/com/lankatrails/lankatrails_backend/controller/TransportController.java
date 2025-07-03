package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transport")
public class TransportController {
    @Autowired
    TransportService transportService;

    @GetMapping("/getAll")
    public ResponseEntity<TransportResponseDTO> getAllTourists(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){
        TransportResponseDTO transportResponseDTO=transportService.getAll(pageNumber,pageSize);
        return  new ResponseEntity<>(transportResponseDTO, HttpStatus.OK);

    }
}
