package com.mediconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MediConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediConnectApplication.class, args);
    }
}