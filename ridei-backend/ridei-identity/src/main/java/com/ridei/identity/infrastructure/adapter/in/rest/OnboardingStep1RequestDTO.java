package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.domain.model.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OnboardingStep1RequestDTO {
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Pattern(
        regexp = "^@[a-zA-Z0-9_.]{3,30}$",
        message = "Username must start with @ followed by 3-30 alphanumeric characters"
    )
    private String username;

    private Gender gender;
}
