package com.lankatrails.lankatrails_backend.controller;


import com.lankatrails.lankatrails_backend.dtos.request.TouristGuideRequestDTO;
import com.lankatrails.lankatrails_backend.dtos.response.TouristGuideResponseDTO;
import com.lankatrails.lankatrails_backend.repositories.TouristGuideRepository;
import com.lankatrails.lankatrails_backend.service.ServicesForAll;
import com.lankatrails.lankatrails_backend.service.TouristGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/guide")
public class TourGuideController {
    @Autowired
    TouristGuideService touristGuideService;

    @Autowired
    ServicesForAll servicesForAll;

    @GetMapping("/all")
    public ResponseEntity<TouristGuideResponseDTO> getAllTourGuides
            (
                    @RequestParam(name = "pageNumber") Integer pageNumber,
                    @RequestParam(name = "pageSize") Integer pageSize
            ){

            TouristGuideResponseDTO touristGuideResponseDTO = touristGuideService.getAllTourGuides();
            return new ResponseEntity<>(touristGuideResponseDTO, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<TouristGuideResponseDTO> getGuideDetails(@PathVariable Long id){
        TouristGuideResponseDTO touristGuideResponseDTO=touristGuideService.getGuideDetails(id);
        return new ResponseEntity<>(touristGuideResponseDTO,HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<TouristGuideResponseDTO> addNewTourGuide(@RequestBody TouristGuideRequestDTO requestDTO){
        TouristGuideResponseDTO touristGuideRequestDTO=touristGuideService.addNewTouristGuide(requestDTO);

        return new ResponseEntity<>(touristGuideRequestDTO,HttpStatus.CREATED);
    }

    @GetMapping("/delete/{id}")
    public String deleteTourGuide(@PathVariable Long id){
        Boolean removeStatus=servicesForAll.removeService(id);
        if (removeStatus)
            return "successfull";
        else
            return "unsuccessfull";

    }
    @PostMapping("/{id}")
    public ResponseEntity<TouristGuideResponseDTO> updateTourGuide(@PathVariable Long id, @RequestBody TouristGuideRequestDTO requestDTO){
        TouristGuideResponseDTO touristGuideResponseDTO=touristGuideService.updateTourGuide(id,requestDTO);
        return  new ResponseEntity<>(touristGuideResponseDTO,HttpStatus.OK);
    }
}
