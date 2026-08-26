package com.ridei.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ridei.identity", "com.ridei.landing"})
@EntityScan(basePackages = {"com.ridei.identity", "com.ridei.landing"})
@EnableJpaRepositories(basePackages = {"com.ridei.identity", "com.ridei.landing"})
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
