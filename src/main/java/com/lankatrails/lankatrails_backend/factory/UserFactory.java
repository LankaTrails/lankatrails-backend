package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.dtos.request.ProviderRegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.RegistrationRequest;
import com.lankatrails.lankatrails_backend.dtos.request.TouristRegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createUser(RegistrationRequest request) {
        return switch (request.getUserRole()) {
            case TOURIST -> createTourist((TouristRegistrationRequest) request);
            case PROVIDER -> createProvider((ProviderRegistrationRequest) request);
            case ADMIN -> throw new UnsupportedOperationException("Admin creation not allowed via registration");
        };
    }

    private Tourist createTourist(TouristRegistrationRequest request) {
        Tourist tourist = new Tourist();
        tourist.setEmail(request.getEmail());
        tourist.setFirstName(request.getFirstName());
        tourist.setLastName(request.getLastName());
        tourist.setCountry(request.getCountry());
        return tourist;
    }

    private Provider createProvider(ProviderRegistrationRequest request) {
        Provider provider = new Provider();
        provider.setEmail(request.getEmail());
        provider.setBusinessName(request.getBusinessName());
        provider.setBusinessDescription(request.getBusinessDescription());
        provider.setLogoUrl(request.getLogoUrl());
        provider.setCategories(request.getCategories());
        return provider;
    }
}