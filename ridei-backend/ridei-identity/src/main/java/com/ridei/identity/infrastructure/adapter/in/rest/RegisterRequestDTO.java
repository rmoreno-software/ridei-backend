package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.model.Email;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(
            new Email(email),
            password
        );
    }

}
