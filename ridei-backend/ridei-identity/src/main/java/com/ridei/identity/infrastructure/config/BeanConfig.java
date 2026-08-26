package com.ridei.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.identity.application.ChangePasswordService;
import com.ridei.identity.application.CheckEmailAvailabilityService;
import com.ridei.identity.application.CheckUsernameAvailabilityService;
import com.ridei.identity.application.ConfirmProfilePictureService;
import com.ridei.identity.application.GetCurrentUserService;
import com.ridei.identity.application.LoginService;
import com.ridei.identity.application.LoginWithGoogleService;
import com.ridei.identity.application.RegisterUserService;
import com.ridei.identity.application.RemoveProfilePictureService;
import com.ridei.identity.application.RequestProfilePictureUploadService;
import com.ridei.identity.application.RequestTemporaryPasswordService;
import com.ridei.identity.application.SaveOnboardingStep1Service;
import com.ridei.identity.application.SaveOnboardingStep2Service;
import com.ridei.identity.application.SaveOnboardingStep4Service;
import com.ridei.identity.application.SaveOnboardingStep5Service;
import com.ridei.identity.application.ValidateTokenService;
import com.ridei.identity.domain.port.in.ChangePasswordUseCase;
import com.ridei.identity.domain.port.in.CheckEmailAvailabilityUseCase;
import com.ridei.identity.domain.port.in.CheckUsernameAvailabilityUseCase;
import com.ridei.identity.domain.port.in.ConfirmProfilePictureUseCase;
import com.ridei.identity.domain.port.in.GetCurrentUserUseCase;
import com.ridei.identity.domain.port.in.LoginUseCase;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.in.RemoveProfilePictureUseCase;
import com.ridei.identity.domain.port.in.RequestProfilePictureUploadUseCase;
import com.ridei.identity.domain.port.in.RequestTemporaryPasswordUseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep1UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep2UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep4UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep5UseCase;
import com.ridei.identity.domain.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.GoogleTokenVerifierPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

@Configuration
public class BeanConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(
        UserRepositoryPort repository,
        EventPublisherPort eventPublisher,
        PasswordHasherPort passwordHasher,
        JwtPort jwt
    ) {
        return new RegisterUserService(repository, eventPublisher, passwordHasher, jwt);
    }

    @Bean
    public LoginWithGoogleUseCase loginWithGoogleUseCase(
        GoogleTokenVerifierPort googleVerifier,
        UserRepositoryPort repository,
        JwtPort jwt,
        EventPublisherPort eventPublisher
    ) {
        return new LoginWithGoogleService(googleVerifier, repository, jwt, eventPublisher);
    }

    @Bean
    public ValidateTokenUseCase validateTokenUseCase(JwtPort jwt, UserRepositoryPort userRepositoryPort) {
        return new ValidateTokenService(jwt, userRepositoryPort);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepositoryPort userRepositoryPort) {
        return new GetCurrentUserService(userRepositoryPort);
    }

    @Bean
    public CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase(UserRepositoryPort repository) {
        return new CheckUsernameAvailabilityService(repository);
    }

    @Bean
    public SaveOnboardingStep1UseCase saveOnboardingStep1UseCase(UserRepositoryPort repository) {
        return new SaveOnboardingStep1Service(repository);
    }

    @Bean
    public SaveOnboardingStep2UseCase saveOnboardingStep2UseCase(UserRepositoryPort repository) {
        return new SaveOnboardingStep2Service(repository);
    }

    @Bean
    public RequestProfilePictureUploadUseCase requestProfilePictureUploadUseCase(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        return new RequestProfilePictureUploadService(userRepository, storage);
    }

    @Bean
    public ConfirmProfilePictureUseCase confirmProfilePictureUseCase(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        return new ConfirmProfilePictureService(userRepository, storage);
    }

    @Bean
    public RemoveProfilePictureUseCase removeProfilePictureUseCase(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        return new RemoveProfilePictureService(userRepository, storage);
    }

    @Bean
    public SaveOnboardingStep4UseCase saveOnboardingStep4UseCase(
        UserRepositoryPort repositoryPort
    ) {
        return new SaveOnboardingStep4Service(repositoryPort);
    }
    
    @Bean
    public SaveOnboardingStep5UseCase saveOnboardingStep5UseCase(
        UserRepositoryPort repositoryPort
    ) {
        return new SaveOnboardingStep5Service(repositoryPort);
    }

    @Bean
    public CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase(
        UserRepositoryPort repositoryPort
    ) {
        return new CheckEmailAvailabilityService(repositoryPort);
    }

    @Bean
    public LoginUseCase loginUseCase(
        UserRepositoryPort repositoryPort,
        PasswordHasherPort passwordHasherPort,
        JwtPort jwt
    ) {
        return new LoginService(repositoryPort, passwordHasherPort, jwt);
    }

    @Bean
    public RequestTemporaryPasswordUseCase requestTemporaryPasswordUseCase(
        UserRepositoryPort repositoryPort,
        PasswordHasherPort passwordHasherPort,
        EmailSenderPort emailSenderPort
    ) {
        return new RequestTemporaryPasswordService(repositoryPort, passwordHasherPort, emailSenderPort);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
        UserRepositoryPort repositoryPort,
        PasswordHasherPort passwordHasherPort
    ) {
        return new ChangePasswordService(repositoryPort, passwordHasherPort);
    }
}
