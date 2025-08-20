package com.lankatrails.lankatrails_backend.dtos;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TouristDto {

    private String firstName;
    private String lastName;
    private Long id;
    

}
