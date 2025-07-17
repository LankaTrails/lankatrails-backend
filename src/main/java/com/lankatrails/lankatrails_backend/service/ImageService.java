package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.model.Services;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    void uploadImagesForService(List<MultipartFile> images, Services service);
}
