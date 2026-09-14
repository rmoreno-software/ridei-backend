package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.domain.model.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OnboardingStep1RequestDTO {
    @NotBlank(message = "{validation.first_name.required}")
    @Size(max = 100, message = "{validation.first_name.size}")
    private String firstName;

    @NotBlank(message = "{validation.last_name.required}")
    @Size(max = 100, message = "{validation.last_name.size}")
    private String lastName;

    @NotBlank(message = "{validation.username.required}")
    @Pattern(
        regexp = "^@[a-zA-Z0-9_.]{3,30}$",
        message = "{validation.username.pattern}"
    )
    private String username;

    private Gender gender;
}
