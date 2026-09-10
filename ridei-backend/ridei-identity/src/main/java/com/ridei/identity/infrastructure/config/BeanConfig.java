package com.ridei.identity.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
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
import com.ridei.identity.application.ResendVerificationEmailService;
import com.ridei.identity.application.ResetPasswordService;
import com.ridei.identity.application.SaveOnboardingStep1Service;
import com.ridei.identity.application.SaveOnboardingStep2Service;
import com.ridei.identity.application.SaveOnboardingStep4Service;
import com.ridei.identity.application.SaveOnboardingStep5Service;
import com.ridei.identity.application.ValidateTokenService;
import com.ridei.identity.application.VerifyEmailService;
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
import com.ridei.identity.domain.port.in.ResendVerificationEmailUseCase;
import com.ridei.identity.domain.port.in.ResetPasswordUseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep1UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep2UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep4UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep5UseCase;
import com.ridei.identity.domain.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.in.VerifyEmailUseCase;
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
        JwtPort jwt,
        EmailSenderPort emailSender,
        @Value("${app.public-api-url}") String publicApiUrl
    ) {
        return new RegisterUserService(
            repository,
            eventPublisher,
            passwordHasher,
            jwt,
            emailSender,
            publicApiUrl
        );
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
    public ValidateTokenUseCase validateTokenUseCase(
        JwtPort jwt,
        UserRepositoryPort userRepository
    ) {
        return new ValidateTokenService(jwt, userRepository);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(
        UserRepositoryPort userRepository
    ) {
        return new GetCurrentUserService(userRepository);
    }

    @Bean
    public CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase(
        UserRepositoryPort repository
    ) {
        return new CheckUsernameAvailabilityService(repository);
    }

    @Bean
    public SaveOnboardingStep1UseCase saveOnboardingStep1UseCase(
        UserRepositoryPort repository
    ) {
        return new SaveOnboardingStep1Service(repository);
    }

    @Bean
    public SaveOnboardingStep2UseCase saveOnboardingStep2UseCase(
        UserRepositoryPort repository
    ) {
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
        UserRepositoryPort repository
    ) {
        return new SaveOnboardingStep4Service(repository);
    }
    
    @Bean
    public SaveOnboardingStep5UseCase saveOnboardingStep5UseCase(
        UserRepositoryPort repository
    ) {
        return new SaveOnboardingStep5Service(repository);
    }

    @Bean
    public CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase(
        UserRepositoryPort repository
    ) {
        return new CheckEmailAvailabilityService(repository);
    }

    @Bean
    public LoginUseCase loginUseCase(
        UserRepositoryPort repository,
        PasswordHasherPort passwordHasher,
        JwtPort jwt
    ) {
        return new LoginService(repository, passwordHasher, jwt);
    }

    @Bean
    public RequestTemporaryPasswordUseCase requestTemporaryPasswordUseCase(
        UserRepositoryPort repository,
        PasswordHasherPort passwordHasher,
        EmailSenderPort emailSender
    ) {
        return new RequestTemporaryPasswordService(repository, passwordHasher, emailSender);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(
        UserRepositoryPort repository,
        PasswordHasherPort passwordHasher
    ) {
        return new ChangePasswordService(repository, passwordHasher);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(
        UserRepositoryPort repository,
        PasswordHasherPort passwordHasher
    ) {
        return new ResetPasswordService(repository, passwordHasher);
    }

    @Bean
    public VerifyEmailUseCase verifyEmailUseCase(UserRepositoryPort userRepository) {
        return new VerifyEmailService(userRepository);
    }

    @Bean 
    public ResendVerificationEmailUseCase resendVerificationEmailUseCase(
        UserRepositoryPort userRepository,
        EmailSenderPort emailSender,
        @Value("${app.public-api-url}") String publicApiUrl
    ) {
        return new ResendVerificationEmailService(userRepository, emailSender, publicApiUrl);
    }
}
