package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.GoogleAuthResult;
import com.ridei.identity.application.LoginWithGoogleCommand;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    
    private final LoginWithGoogleUseCase useCase;

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDTO> loginWithGoogle(
        @RequestBody @Valid GoogleAuthRequestDTO dto
    ) {
        GoogleAuthResult result = useCase.login(new LoginWithGoogleCommand(dto.getIdToken()));
        return ResponseEntity.ok(new GoogleAuthResponseDTO(
            result.accessToken(),
            result.refreshToken(),
            result.needsOnboarding()
        ));
    }
}
