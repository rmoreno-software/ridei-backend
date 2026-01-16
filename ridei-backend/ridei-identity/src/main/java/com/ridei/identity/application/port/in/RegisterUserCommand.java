package com.ridei.identity.application.port.in;

import com.ridei.identity.shared.SelfValidating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegisterUserCommand extends SelfValidating<RegisterUserCommand> {

    @NotBlank(message = "The email address is invalid")
    private final String email;

    @NotBlank(message = "The password is required")
    @Size(min = 6, message = "The password must be at least 6 characters long")
    private final String password;

    @NotBlank(message = "The name is required")
    private final String name;

    public RegisterUserCommand(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;

        this.validateSelf();
    }

}
