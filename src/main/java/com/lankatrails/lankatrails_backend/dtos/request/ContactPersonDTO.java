package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.*;

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
