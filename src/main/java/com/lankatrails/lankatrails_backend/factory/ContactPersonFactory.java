package com.lankatrails.lankatrails_backend.factory;

import com.lankatrails.lankatrails_backend.dtos.request.ContactPersonDTO;
import com.lankatrails.lankatrails_backend.model.ContactPerson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContactPersonFactory {

    public ContactPerson createFromDTO(ContactPersonDTO contactPersonDTO) {
        return ContactPerson.builder()
                .name(contactPersonDTO.getName())
                .email(contactPersonDTO.getEmail())
                .phoneNumber(contactPersonDTO.getPhoneNumber())
                .position(contactPersonDTO.getPosition())
                .identityDocumentUrl(contactPersonDTO.getIdentityDocumentUrl())
                .build();
    }
}
