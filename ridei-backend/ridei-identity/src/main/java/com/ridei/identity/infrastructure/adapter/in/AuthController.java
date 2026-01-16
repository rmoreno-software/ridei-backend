package com.ridei.identity.infrastructure.adapter.in;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridei.identity.application.port.in.RegisterUserCommand;
import com.ridei.identity.application.port.in.RegisterUserUseCase;
import com.ridei.identity.infrastructure.adapter.in.web.dto.ApiResponse;
import com.ridei.identity.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ridei.identity.infrastructure.adapter.in.web.mapper.AuthMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthMapper authMapper;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthMapper authMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.authMapper = authMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {

        RegisterUserCommand command = authMapper.toCommand(request);

        registerUserUseCase.register(command);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "Successfully registered user"));
    }
}
