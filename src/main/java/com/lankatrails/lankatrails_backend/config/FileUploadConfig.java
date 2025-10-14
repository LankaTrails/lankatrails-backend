package com.lankatrails.lankatrails_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Data
public class FileUploadConfig {

    private String rootPath;

}
