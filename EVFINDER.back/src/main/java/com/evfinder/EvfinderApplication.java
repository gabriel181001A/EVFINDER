package com.evfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EvfinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvfinderApplication.class, args);
    }

}