package com.lankatrails.lankatrails_backend.dtos.request;

import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProviderRegistrationRequest extends RegistrationRequest {
    @Size(max = 50)
    private String businessName;

    @Size(max = 100)
    private String businessDescription;

    @Size(max = 255)
    private String logoUrl;

//    @Setter
//    @Getter
//    private Set<String> categories;

    public ProviderRegistrationRequest() {
        this.setUserRole(UserRole.ROLE_PROVIDER);
    }

    public @Size(max = 50) String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(@Size(max = 50) String businessName) {
        this.businessName = businessName;
    }

    public @Size(max = 100) String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(@Size(max = 100) String businessDescription) {
        this.businessDescription = businessDescription;
    }

    public @Size(max = 255) String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(@Size(max = 255) String logoUrl) {
        this.logoUrl = logoUrl;
    }

}