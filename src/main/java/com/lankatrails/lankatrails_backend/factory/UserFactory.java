package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.model.*;
import com.lankatrails.lankatrails_backend.model.enums.*;
import com.lankatrails.lankatrails_backend.dtos.request.*;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final CategoryRepository categoryRepository;

    public User createUser(TouristRegistrationRequest request) {
        Tourist tourist = new Tourist();
        tourist.setEmail(request.getEmail().toLowerCase());
        tourist.setFirstName(request.getFirstName());
        tourist.setLastName(request.getLastName());
        tourist.setCountry(request.getCountry().toLowerCase());
        tourist.setRole(UserRole.ROLE_TOURIST);
        return tourist;
    }

    public User createUser(ProviderRegistrationRequest request) {
        Provider provider = new Provider();
        provider.setEmail(request.getEmail().toLowerCase());
        provider.setBusinessName(request.getBusinessName());
        provider.setBusinessDescription(request.getBusinessDescription());
        provider.setRole(UserRole.ROLE_PROVIDER);
        return provider;
    }

    private Set<Category> mapCategories(Set<String> categoryNames) {
        Set<Category> categories = new HashSet<>();

        categoryNames.forEach(categoryName -> {
            ServiceCategory serviceCategory = ServiceCategory.valueOf(categoryName.toUpperCase());
            Category category = categoryRepository.findByCategoryName(serviceCategory)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + categoryName));
            categories.add(category);
        });

        return categories;
    }
}