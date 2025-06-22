package com.lankatrails.lankatrails_backend.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class ServiceRequest {
    private String service_name;
    private String location_based;
    private String contact_no;
    private Boolean status;
}
