package com.ridei.landing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.landing.application.JoinWaitlistService;
import com.ridei.landing.domain.port.in.JoinWaitlistUseCase;
import com.ridei.landing.domain.port.in.WaitlistRepositoryPort;

@Configuration
public class LandingBeanConfig {
    @Bean
    public JoinWaitlistUseCase joinWaitlistUseCase(WaitlistRepositoryPort repositoryPort) {
        return new JoinWaitlistService(repositoryPort);
    }
}
