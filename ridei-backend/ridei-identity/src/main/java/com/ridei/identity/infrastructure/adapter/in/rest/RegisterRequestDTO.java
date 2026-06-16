package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.application.RegisterUserCommand;
import com.ridei.identity.domain.model.*;
import com.ridei.identity.domain.model.Email;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^@[a-zA-Z0-9_.]{3,30}$", message = "Username must start with @ followed by 3-30 alphanumeric characters")
    private String username;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Gender gender;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be ISO 3166-1 alpha-2 (e.g. ES, IT, FR)")
    private String countryCode;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotNull(message = "Role is required")
    private UserRole role;

    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean termsAccepted;

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(
            new Email(email),
            password,
            new Username(username),
            firstName,
            lastName,
            gender,
            new PhoneNumber(phoneNumber),
            dateOfBirth,
            countryCode,
            new IdentityDocument(documentType, documentNumber),
            role,
            termsAccepted
        );
    }

}
