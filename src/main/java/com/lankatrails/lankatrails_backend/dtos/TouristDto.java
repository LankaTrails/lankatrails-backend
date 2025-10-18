package com.lankatrails.lankatrails_backend.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String country;
    private String profilePictureUrl;
}
