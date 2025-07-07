package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ProviderProfileDto extends UserProfileDto {

    private String businessName;
    private String businessDescription;
    private String logoUrl;
//    private String profilePictureUrl;
//    private String bio;
}
