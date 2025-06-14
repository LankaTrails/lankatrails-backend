package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createUser(TouristRegistrationRequest request) {
        Tourist tourist = new Tourist();
        tourist.setEmail(request.getEmail());
        tourist.setFirstName(request.getFirstName());
        tourist.setLastName(request.getLastName());
        tourist.setCountry(request.getCountry());
        tourist.setRole(UserRole.TOURIST);
        return tourist;
    }

    public User createUser(ProviderRegistrationRequest request) {
        Provider provider = new Provider();
        provider.setEmail(request.getEmail());
        provider.setBusinessName(request.getBusinessName());
        provider.setBusinessDescription(request.getBusinessDescription());
        provider.setLogoUrl(request.getLogoUrl());
        provider.setCategories(request.getCategories());
        provider.setRole(UserRole.PROVIDER);
        return provider;
    }
}