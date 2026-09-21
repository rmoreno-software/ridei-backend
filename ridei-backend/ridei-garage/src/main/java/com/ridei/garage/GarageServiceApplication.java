package com.ridei.garage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication (exclude = UserDetailsServiceAutoConfiguration.class)
public class GarageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GarageServiceApplication.class, args);
    }
}
