package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.service.TransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/provider/transport")
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
    @GetMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> searchWithId(@PathVariable Long id){
        TransportResponseDTO transportResponseDTO=transportService.getById(id);
        return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> updateTransport(
            @PathVariable Long id,
            @RequestBody TransportRequestDTO transportRequestDTO
            ){
        TransportResponseDTO transportResponseDTO=transportService.updateTransport(id,transportRequestDTO);
        return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<TransportResponseDTO> addNewTransport(
            @RequestBody TransportRequestDTO transportRequestDTO
    ){
        TransportResponseDTO transportResponseDTO=transportService.addNewTransport(transportRequestDTO);
        return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);

    }
    @PutMapping("/remove/{id}")
    public ResponseEntity<APIResponse<String>> deleteTransport(@PathVariable Long id){
        APIResponse<String> response=transportService.deleteTransport(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
