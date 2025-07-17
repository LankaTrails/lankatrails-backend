package com.lankatrails.lankatrails_backend.controller;

import com.lankatrails.lankatrails_backend.dtos.request.TransportRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.TransportResponseDTO;
import com.lankatrails.lankatrails_backend.service.TransportService;
import io.vavr.API;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/provider/transport")
public class TransportController {
    @Autowired
    TransportService transportService;

    @GetMapping("/getAll")
    public ResponseEntity<APIResponse<TransportResponseDTO>> getAllTourists(
            @RequestParam(name = "pageNumber") Integer pageNumber,
            @RequestParam(name = "pageSize") Integer pageSize
    ){
        APIResponse<TransportResponseDTO> transportResponseDTO=transportService.getAll(pageNumber,pageSize);
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
    @PostMapping(value = "/add",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<String>> addNewTransport(
            @RequestPart("service") @Valid  TransportRequestDTO transportRequestDTO,
            @RequestPart(value = "images", required = false)List<MultipartFile> images,
            BindingResult result
            ){
        if (result.hasErrors()){
            Map<String,String> errors = new HashMap<>();
            result.getFieldErrors().forEach(field ->{
                errors.put(field.getField(), field.getDefaultMessage());
            });
            APIResponse<String> errorResponse = APIResponse.<String>builder()
                    .success(false)
                    .message("Validation Failed")
                    .details(errors)
                    .build();
            return  new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
        }else{
            APIResponse<String> transportResponseDTO=transportService.addNewTransport(transportRequestDTO,images);
            return new ResponseEntity<>(transportResponseDTO,HttpStatus.OK);
        }


    }
    @PutMapping("/remove/{id}")
    public ResponseEntity<APIResponse<String>> deleteTransport(@PathVariable Long id){
        APIResponse<String> response=transportService.deleteTransport(id);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
