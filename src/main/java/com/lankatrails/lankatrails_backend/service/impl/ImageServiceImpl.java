package com.lankatrails.lankatrails_backend.service.impl;

import com.lankatrails.lankatrails_backend.dtos.request.ImageRequestDTO;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import com.lankatrails.lankatrails_backend.repositories.ImageRepository;
import com.lankatrails.lankatrails_backend.service.ImageService;
import com.lankatrails.lankatrails_backend.service.utils.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ImageServiceImpl implements ImageService {
    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public void uploadImagesForService(List<MultipartFile> images, Service service) {
        // Check if images list is null or empty
        if (images == null || images.isEmpty()) {
            return; // Exit early if no images to process
        }

        List<Image> imageList = new ArrayList<>();
        for (MultipartFile file : images) {
            // Additional null check for individual file
            if (file != null && !file.isEmpty()) {
                String imageUrl = fileUploadService.storeFile(file, UploadCategory.SERVICE_PICTURE, "service");

                Image image = new Image();
                image.setImageUrl(imageUrl);
                image.setService(service);

                imageList.add(image); // Collect images
            }
        }
        // Persist images only if we have images to save
        if (!imageList.isEmpty()) {
            imageRepository.saveAll(imageList);
        }
    }

    @Override
    public void deleteImages(List<ImageRequestDTO> imageRequestDTOs) {
        List<Long> imageIds = new ArrayList<>();
        for (ImageRequestDTO imageRequestDTO : imageRequestDTOs) {
            imageIds.add(imageRequestDTO.getId());
        }

        List<Image> imagesToDelete = imageRepository.findAllById(imageIds);
        if (!imagesToDelete.isEmpty()) {
            for (Image image : imagesToDelete) {
                fileUploadService.deleteFile(image.getImageUrl(), UploadCategory.SERVICE_PICTURE, "service");
            }
            imageRepository.deleteAll(imagesToDelete);
        }
    }
}
