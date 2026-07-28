package com.ridei.identity.infrastructure.adapter.in.rest;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OnboardingStep2RequestDTO {
    
    @NotBlank(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 3, message = "Country code must be format 34, 44, etc.")
    private String countryCode;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{6,14}$",
        message = "Invalid phone number format"
    )
    private String phoneNumber;
}
