package com.ridei.identity.infrastructure.adapter.in.web;

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
import lombok.RequiredArgsConstructor;

/**
 * Primary Adapter (Driving Adapter) exposing Authentication endpoints via REST.
 * <p>
 * This controller serves as the entry point for the "Web Adapter" into the Hexagonal Application.
 * It is responsible for:
 * <ul>
 * <li>Handling HTTP Protocol specifics (Status codes, Headers, Body parsing).</li>
 * <li>Validating the syntactic structure of incoming requests ({@code @Valid}).</li>
 * <li>Mapping external DTOs to internal Domain Commands.</li>
 * <li>Invoking the Input Port (Use Case).</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * The Input Port (Use Case) interface.
     * We depend on the abstraction, not the implementation (Dependency Inversion).
     */
    private final RegisterUserUseCase registerUserUseCase;

    /**
     * The Infrastructure Mapper to convert DTOs -> Commands.
     */
    private final AuthMapper authMapper;

    /**
     * Public endpoint to register a new user in the system.
     * <p>
     * <b>Flow:</b>
     * <ol>
     * <li>Receives JSON payload.</li>
     * <li>Validates format (Email, Non-nulls) via {@code @Valid}.</li>
     * <li>Maps {@link RegisterRequest} to {@link RegisterUserCommand}.</li>
     * <li>Delegates processing to the Domain Service.</li>
     * <li>Returns HTTP 201 (Created) on success.</li>
     * </ol>
     * </p>
     *
     * @param request The validated registration payload containing email, password, and name.
     * @return A {@link ResponseEntity} containing the standard API envelope with HTTP 201 status.
     * @see com.ridei.identity.infrastructure.adapter.in.web.advice.GlobalExceptionHandler for error handling scenarios.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {

        // 1. Adapter Responsibility: Translation (DTO -> Command)
        RegisterUserCommand command = authMapper.toCommand(request);

        // 2. Drive the Domain (Input Port)
        registerUserUseCase.register(command);

        // 3. Adapter Responsibility: Protocol Response (HTTP 201)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(201, "Successfully registered user"));
    }
}
