package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface ImageService {
    void uploadImagesForService(List<MultipartFile> images, Service service);
}
