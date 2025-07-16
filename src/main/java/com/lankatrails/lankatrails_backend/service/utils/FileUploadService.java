package com.lankatrails.lankatrails_backend.service.utils;

import com.lankatrails.lankatrails_backend.exception.FileUploadException;
import com.lankatrails.lankatrails_backend.model.Image;
import com.lankatrails.lankatrails_backend.model.enums.UploadCategory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_ROOT = "E:\\LankaTrails\\lankatrails-backend\\uploads";
//    private static final String UPLOAD_ROOT = "D:\\LankaTrails\\lankatrails\\lankatrails-backend\\uploads";

    public String storeFile(MultipartFile file, UploadCategory category, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Build directory path
            Path dirPath = Paths.get(UPLOAD_ROOT, category.getDirectory());
            Files.createDirectories(dirPath);

            // Extract extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    extension = originalFilename.substring(dotIndex);
                }
            }

            // Generate unique filename
            String uniqueFilename = UUID.randomUUID().toString() + (prefix != null ? "_" + prefix : "") + extension;

            // Save file
            Path filePath = dirPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            // Return relative URL path
            return "/uploads" + "/" + category.getDirectory() + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new FileUploadException("Failed to store file: " + e.getMessage());
        }
    }

    //method to store list of files
    public Set<String> storeFiles(MultipartFile[] files, UploadCategory category) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files to upload");
        }

        String[] fileUrls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileUrls[i] = storeFile(files[i], category, null);
        }

        return Set.of(fileUrls);
    }

    public Set<Image> storeImages(MultipartFile[] files, UploadCategory category) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files to upload");
        }

        Set<Image> images = Set.of();
        for (MultipartFile file : files) {
            String fileUrl = storeFile(file, category, null);
            Image image = new Image();
            image.setImageUrl(fileUrl);
            images.add(image);
        }
        return images;
    }
}
