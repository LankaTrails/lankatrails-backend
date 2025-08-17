package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class AdminProfileDto extends UserProfileDto {
    private String firstName;
    private String lastName;
}
