package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.request.LicenseDTO;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LicenseFactory {
    private final CategoryRepository categoryRepository;

    public License createFromDTO(LicenseDTO licenseDTO) {
        License license = new License();
        license.setLicenseNumber(licenseDTO.getLicenseNumber());
        license.setExpiryDate(licenseDTO.getExpiryDate());
        license.setLicenseUrl(licenseDTO.getLicenseUrl());
        Category category = categoryRepository.findByCategoryName(licenseDTO.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + licenseDTO.getCategory()));
        license.setCategory(category);

        return license;
    }
}
