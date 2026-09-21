package com.ridei.garage.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.garage.application.ListMyMotorbikeService;
import com.ridei.garage.application.RegisterMotorbikeService;
import com.ridei.garage.domain.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

@Configuration 
public class BeanConfig {

    @Bean 
    public RegisterMotorbikeUseCase registerMotorbikeUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new RegisterMotorbikeService(motorbikeRepository);
    }

    @Bean 
    public ListMyMotorbikesUseCase listMyMotorbikesUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new ListMyMotorbikeService(motorbikeRepository);
    }
    
}
