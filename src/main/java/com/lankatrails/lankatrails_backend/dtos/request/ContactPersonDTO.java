package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactPersonDTO {
    private String name;
    private String email;
    private String phoneNumber;
    private String position;
    private String identityDocumentUrl;
}
