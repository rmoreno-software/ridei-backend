package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.RequestProfilePictureUploadCommand;
import com.ridei.identity.application.SaveOnboardingStep1Command;
import com.ridei.identity.application.SaveOnboardingStep2Command;
import com.ridei.identity.application.UsernameAvailabilityResult;
import com.ridei.identity.domain.model.ImageContentType;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.PresignedUpload;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.model.Username;
import com.ridei.identity.domain.port.in.CheckUsernameAvailabilityUseCase;
import com.ridei.identity.domain.port.in.GetCurrentUserUseCase;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.in.RequestProfilePictureUploadUseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep1UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep2UseCase;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;
    private final SaveOnboardingStep1UseCase saveOnboardingStep1UseCase;
    private final SaveOnboardingStep2UseCase saveOnboardingStep2UseCase;
    private final RequestProfilePictureUploadUseCase requestProfilePictureUploadUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        UserId id = registerUserUseCase.register(dto.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponseDTO(id.value().toString()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> me(Authentication authentication) {
        UserId userId = UserId.of(authentication.getName());
        UserProfile profile = getCurrentUserUseCase.get(userId);
        return ResponseEntity.ok(UserProfileResponseDTO.fromDomain(profile));
    }

    @PatchMapping("/me/onboarding-stepone")
    public ResponseEntity<OnboardingResponseDTO> saveOnboardingStepOne(
        @RequestBody @Valid OnboardingStep1RequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());
        
        SaveOnboardingStep1Command command = new SaveOnboardingStep1Command(
            userId,
            dto.getFirstName(),
            dto.getLastName(),
            new Username(dto.getUsername()),
            dto.getGender()
        );

        saveOnboardingStep1UseCase.complete(command);

        return ResponseEntity.ok(OnboardingResponseDTO.success());
    }

    @PatchMapping("/me/onboarding-steptwo")
    public ResponseEntity<OnboardingResponseDTO> saveOnboardingStepTwo(
        @RequestBody @Valid OnboardingStep2RequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());

        SaveOnboardingStep2Command command = new SaveOnboardingStep2Command(
            userId,
            dto.getDateOfBirth(),
            dto.getCountryCode(),
            new PhoneNumber(dto.getPhoneNumber())
        );

        saveOnboardingStep2UseCase.complete(command);

        return ResponseEntity.ok(OnboardingResponseDTO.success());
    }

    @GetMapping("/username-availability")
    public ResponseEntity<UsernameAvailabilityResponseDTO> checkUsernameAvailability(
        @RequestParam String username
    ) {
        UsernameAvailabilityResult result = checkUsernameAvailabilityUseCase.check(username);
        return ResponseEntity.ok(UsernameAvailabilityResponseDTO.fromResult(result));
    }

    @PostMapping("/me/profile-picture/upload-url")
    public ResponseEntity<ProfilePictureUploadResponseDTO> requestProfilePictureUploadUrl(
        @RequestBody @Valid ProfilePictureUploadRequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());

        RequestProfilePictureUploadCommand command = new RequestProfilePictureUploadCommand(
            userId,
            new ImageContentType(dto.getContentType())
        );

        PresignedUpload upload = requestProfilePictureUploadUseCase.request(command);

        return ResponseEntity.ok(ProfilePictureUploadResponseDTO.fromDomain(upload));
    }

}
