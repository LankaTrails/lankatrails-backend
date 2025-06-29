package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/tourist")
public class TourGuideController {
    @Autowired
    TouristGuideService touristGuideService;

    @GetMapping("/all")
    public ResponseEntity<TouristGuideResponseDTO> getAllTourGuides
            (
                    @RequestParam(name = "pageNumber") Integer pageNumber,
                    @RequestParam(name = "pageSize") Integer pageSize
            ){

            TouristGuideResponseDTO touristGuideResponseDTO = touristGuideService.getAllTourGuides();
            return new ResponseEntity<>(touristGuideResponseDTO, HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<TouristGuideResponseDTO> addNewTourGuide(@RequestBody TouristGuideRequestDTO requestDTO){
        TouristGuideResponseDTO touristGuideRequestDTO=touristGuideService.addNewTouristGuide(requestDTO);
        return null;
    }
}
