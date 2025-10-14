package com.lankatrails.lankatrails_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = fileUploadConfig.getRootPath();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}

