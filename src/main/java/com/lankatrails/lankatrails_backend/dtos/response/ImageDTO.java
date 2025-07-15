package com.lankatrails.lankatrails_backend.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDTO {
    private Long id;
    private String imageUrl;
    private String serviceName;

    public ImageDTO(Long id, String imageUrl, String serviceName) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.serviceName = serviceName;
    }
}
