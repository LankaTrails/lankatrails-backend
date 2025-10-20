package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.UserPreferencesDTO;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
//@JsonTypeInfo(
//        use = JsonTypeInfo.Id.NAME,
//        include = JsonTypeInfo.As.PROPERTY,
//        property = "role"
//)
//@JsonSubTypes({
//        @JsonSubTypes.Type(value = TouristProfileDto.class, name = "ROLE_TOURIST"),
//        @JsonSubTypes.Type(value = ProviderProfileDto.class, name = "ROLE_PROVIDER")
//})
public abstract class UserProfileDto {
    private Long id;
    private String email;
    private UserRole role;
    private UserStatus status;
    private String profilePictureUrl;
    private boolean emailVerified;
    private UserPreferencesDTO userPreferences;
}
