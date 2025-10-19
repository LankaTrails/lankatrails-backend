package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.BusinessDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessResponseDTO {
    private BusinessDetailDTO content;
}
