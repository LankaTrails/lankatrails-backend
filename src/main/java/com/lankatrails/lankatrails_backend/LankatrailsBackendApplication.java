package com.lankatrails.lankatrails_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LankatrailsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LankatrailsBackendApplication.class, args);
        System.out.println("Lankatrails Backend Application is running!");
    }

}
