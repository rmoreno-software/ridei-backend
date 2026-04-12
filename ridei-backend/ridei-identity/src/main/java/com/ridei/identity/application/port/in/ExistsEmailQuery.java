package com.ridei.identity.application.port.in;

import com.ridei.identity.shared.SelfValidating;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ExistsEmailQuery extends SelfValidating<ExistsEmailQuery>{

    @NotBlank(message = "Nickname can't be empty")
    @Email(message = "Invalid email format")
    private final String email;
    
    public ExistsEmailQuery(String email) {
        this.email = email;
        this.validateSelf();
    }
}
