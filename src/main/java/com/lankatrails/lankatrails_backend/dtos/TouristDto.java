package com.lankatrails.lankatrails_backend.dtos;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristDto {
    private String firstName;
    private String lastName;
    private Long id;
    private String profileImage;
}
