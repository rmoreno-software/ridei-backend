package com.ridei.identity.infrastructure.adapter.in.rest;

import java.util.Locale;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.model.Email;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "{validation.email.required}")
    @jakarta.validation.constraints.Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, message = "{validation.password.min_size}")
    private String password;

    public RegisterUserCommand toCommand(Locale locale) {
        return new RegisterUserCommand(
            new Email(email),
            password,
            locale
        );
    }

}
