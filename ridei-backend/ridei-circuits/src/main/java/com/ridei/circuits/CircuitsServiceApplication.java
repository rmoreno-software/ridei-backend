package com.ridei.circuits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CircuitsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircuitsServiceApplication.class, args);
    }
}
