package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.UsernameAvailabilityResult;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.port.in.CheckUsernameAvailabilityUseCase;
import com.ridei.identity.domain.port.in.GetCurrentUserUseCase;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;

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

    @GetMapping("/username-availability")
    public ResponseEntity<UsernameAvailabilityResponseDTO> checkUsernameAvailability(
        @RequestParam String username
    ) {
        UsernameAvailabilityResult result = checkUsernameAvailabilityUseCase.check(username);
        return ResponseEntity.ok(UsernameAvailabilityResponseDTO.fromResult(result));
    }

}
