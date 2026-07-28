package com.ridei.identity.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OnboardingStep2RequestDTO {
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Country code is required")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{0,3}$",
        message = "Invalid country code. Use numeric format (e.g. 34, 1, 44)"
    )
    private String countryCode;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{6,14}$",
        message = "Invalid phone number format"
    )
    private String phoneNumber;
}
