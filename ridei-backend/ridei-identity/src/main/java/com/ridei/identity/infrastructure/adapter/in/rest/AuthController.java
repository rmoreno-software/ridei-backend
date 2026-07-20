package com.ridei.identity.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.GoogleAuthResult;
import com.ridei.identity.application.LoginWithGoogleCommand;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.in.ValidateTokenUseCase;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final LoginWithGoogleUseCase loginWithGoogleUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;

    public AuthController(
        LoginWithGoogleUseCase loginWithGoogleUseCase,
        ValidateTokenUseCase validateTokenUseCase) {

        this.loginWithGoogleUseCase = loginWithGoogleUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
    }

    @PostMapping("/google")
    public ResponseEntity<GoogleAuthResponseDTO> loginWithGoogle(
        @RequestBody @Valid GoogleAuthRequestDTO dto
    ) {
        GoogleAuthResult result = loginWithGoogleUseCase.login(new LoginWithGoogleCommand(dto.getIdToken()));
        return ResponseEntity.ok(new GoogleAuthResponseDTO(
            result.accessToken(),
            result.refreshToken(),
            result.needsOnboarding()
        ));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        boolean valid = validateTokenUseCase.validate(token);

        return valid
            ? ResponseEntity.ok().build()
            : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
