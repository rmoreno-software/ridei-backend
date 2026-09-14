package com.ridei.identity.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OnboardingStep2RequestDTO {
    
    @NotNull(message = "{validation.date_of_birth.required}")
    @Past(message = "{validation.date_of_birth.past}")
    private LocalDate dateOfBirth;

    @NotBlank(message = "{validation.country_code.required}")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{0,3}$",
        message = "{validation.country_code.pattern}"
    )
    private String countryCode;

    @NotBlank(message = "{validation.phone_number.required}")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{6,14}$",
        message = "{validation.phone_number.pattern}"
    )
    private String phoneNumber;
}
