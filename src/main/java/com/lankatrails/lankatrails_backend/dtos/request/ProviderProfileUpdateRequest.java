package com.lankatrails.lankatrails_backend.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Editable parts of the provider's public profile. */
@Getter
@Setter
@NoArgsConstructor
public class ProviderProfileUpdateRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255)
    private String businessName;

    @Size(max = 255, message = "Business description must be at most 255 characters")
    private String businessDescription;

    /** Optional; replaces the provider's location when present. */
    @Valid
    private LocationDTO location;
}
