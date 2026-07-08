package com.ridei.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.identity.application.LoginWithGoogleService;
import com.ridei.identity.application.RegisterUserService;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.GoogleTokenVerifierPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
        UserRepositoryPort reporitory,
        EventPublisherPort eventPublisher,
        PasswordHasherPort passwordHasher
    ) {
        return new RegisterUserService(reporitory, eventPublisher, passwordHasher);
    }

    @Bean
    public LoginWithGoogleUseCase loginWithGoogleUseCase(
        GoogleTokenVerifierPort googleVerifier,
        UserRepositoryPort repository,
        JwtPort jwt
    ) {
        return new LoginWithGoogleService(googleVerifier, repository, jwt);
    }
}
