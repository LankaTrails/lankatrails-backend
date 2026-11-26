package com.lankatrails.lankatrails_backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /** Injected wherever "now" matters, so time-dependent logic can be tested. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
