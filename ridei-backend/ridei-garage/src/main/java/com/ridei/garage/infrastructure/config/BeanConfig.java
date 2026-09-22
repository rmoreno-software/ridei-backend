package com.ridei.garage.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.garage.application.ActivateMotorbikeService;
import com.ridei.garage.application.DeactivateMotorbikeService;
import com.ridei.garage.application.ListMyMotorbikeService;
import com.ridei.garage.application.RegisterMotorbikeService;
import com.ridei.garage.domain.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.in.DeactivateMotorbikeUseCase;
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

    @Bean 
    public ActivateMotorbikeUseCase activateMotorbikeUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new ActivateMotorbikeService(motorbikeRepository);
    }

    @Bean 
    public DeactivateMotorbikeUseCase deactivateMotorbikeUseCase(MotorbikeRepositoryPort motorbikeRepository) {
        return new DeactivateMotorbikeService(motorbikeRepository);
    }
    
}
