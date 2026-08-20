package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ridei.identity.application.ConfirmProfilePictureCommand;
import com.ridei.identity.application.EmailAvailabilityResult;
import com.ridei.identity.application.RemoveProfilePictureCommand;
import com.ridei.identity.application.RequestProfilePictureUploadCommand;
import com.ridei.identity.application.SaveOnboardingStep1Command;
import com.ridei.identity.application.SaveOnboardingStep2Command;
import com.ridei.identity.application.SaveOnboardingStep4Command;
import com.ridei.identity.application.SaveOnboardingStep5Command;
import com.ridei.identity.application.UsernameAvailabilityResult;
import com.ridei.identity.domain.model.IdentityDocument;
import com.ridei.identity.domain.model.ImageContentType;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.PresignedUpload;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.model.Username;
import com.ridei.identity.domain.port.in.CheckEmailAvailabilityUseCase;
import com.ridei.identity.domain.port.in.CheckUsernameAvailabilityUseCase;
import com.ridei.identity.domain.port.in.ConfirmProfilePictureUseCase;
import com.ridei.identity.domain.port.in.GetCurrentUserUseCase;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.in.RemoveProfilePictureUseCase;
import com.ridei.identity.domain.port.in.RequestProfilePictureUploadUseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep1UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep2UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep4UseCase;
import com.ridei.identity.domain.port.in.SaveOnboardingStep5UseCase;

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
    private final ConfirmProfilePictureUseCase confirmProfilePictureUseCase;
    private final RemoveProfilePictureUseCase removeProfilePictureUseCase;
    private final SaveOnboardingStep4UseCase saveOnboardingStep4UseCase;
    private final SaveOnboardingStep5UseCase saveOnboardingStep5UseCase;
    private final CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;

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

    @PatchMapping("/me/profile-picture")
    public ResponseEntity<ProfilePictureConfirmationResponseDTO> confirmProfilePicture(
        @RequestBody @Valid ConfirmProfilePictureRequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());

        ConfirmProfilePictureCommand command = new ConfirmProfilePictureCommand(userId, dto.getPublicUrl());

        confirmProfilePictureUseCase.confirm(command);

        return ResponseEntity.ok(ProfilePictureConfirmationResponseDTO.success());
    }

    @DeleteMapping("/me/profile-picture")
    public ResponseEntity<ProfilePictureConfirmationResponseDTO> removeProfilePicture(Authentication authentication) {
        UserId userId = UserId.of(authentication.getName());

        RemoveProfilePictureCommand command = new RemoveProfilePictureCommand(userId);

        removeProfilePictureUseCase.remove(command);

        return ResponseEntity.ok(ProfilePictureConfirmationResponseDTO.removed());
    }

    @PatchMapping("/me/onboarding-stepfour")
    public ResponseEntity<OnboardingResponseDTO> saveOnboardingStepFour(
        @RequestBody @Valid OnboardingStep4RequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());

        SaveOnboardingStep4Command command = new SaveOnboardingStep4Command(
            userId,
            new IdentityDocument(DocumentTypeCodec.decode(dto.getDocumentType()), dto.getDocumentNumber())
        );

        saveOnboardingStep4UseCase.complete(command);

        return ResponseEntity.ok(OnboardingResponseDTO.success());
    }

    @PatchMapping("/me/onboarding-stepfive")
    public ResponseEntity<OnboardingResponseDTO> saveOnboardingStep5(
        @RequestBody @Valid OnboardingStep5RequestDTO dto,
        Authentication authentication
    ) {
        UserId userId = UserId.of(authentication.getName());

        SaveOnboardingStep5Command command = new SaveOnboardingStep5Command(userId);

        saveOnboardingStep5UseCase.complete(command);

        return ResponseEntity.ok(OnboardingResponseDTO.success());
    }

    @GetMapping("/email-availability")
    public ResponseEntity<EmailAvailabilityResponseDTO> checkEmailAvailability(
        @RequestParam String email
    ) {
        EmailAvailabilityResult result = checkEmailAvailabilityUseCase.check(email);
        return ResponseEntity.ok(EmailAvailabilityResponseDTO.fromResult(result));
    }

}
