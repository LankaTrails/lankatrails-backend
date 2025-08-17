package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.ImageRequestDTO;
import com.lankatrails.lankatrails_backend.model.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    void uploadImagesForService(List<MultipartFile> images, Service service);
    void deleteImages(List<ImageRequestDTO> imageRequestDTOs);
}
