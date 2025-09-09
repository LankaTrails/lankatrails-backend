package com.lankatrails.lankatrails_backend.dtos.response;

import com.lankatrails.lankatrails_backend.dtos.request.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDTO {
    private List<ProviderSearchDTO> providers;
    private List<ServiceDTO> services;
}
