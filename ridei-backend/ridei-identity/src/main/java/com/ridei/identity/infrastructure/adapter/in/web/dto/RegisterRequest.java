package com.ridei.identity.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Input Data Transfer Object (DTO) representing the registration payload.
 * <p>
 * This record defines the JSON structure expected by the {@code POST /auth/register} endpoint.
 * It acts as the <b>first line of defense</b>, utilizing Jakarta Bean Validation to perform
 * <b>syntactic validation</b> (e.g., ensuring the email looks like an email) before the
 * request consumes any domain resources.
 * </p>
 *
 * @param email    The user's email address. Must be a valid format (e.g., user@example.com).
 * @param password The raw, plain-text password. Must meet minimum length requirements.
 * @param name     The user's full display name.
 */
public record RegisterRequest(

    /**
     * The user's email address.
     * <p>
     * <b>Validation:</b> Checked for non-blank value and adherence to RFC 5322 email format.
     * </p>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "The email format is invalid")
    String email,
    
    /**
     * The confidential password string.
     * <p>
     * <b>Security Validation:</b> Enforces a minimum length of 6 characters to prevent
     * weak passwords.
     * </p>
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "The password must be at least 6 characters long")
    String password,
    
    /**
     * The public display name of the user.
     */
    @NotBlank(message = "Name is required")
    String name
) {

    /**
     * Security Override: Masks the password in logs.
     * <p>
     * By default, Java Records print all fields in {@code toString()}.
     * This override ensures that if this DTO is accidentally logged,
     * the password remains confidential.
     * </p>
     *
     * @return A string representation of the request with the password redacted.
     */
    @Override
    public String toString() {
        return "RegisterRequest[" +
                "email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", password='*****']";
    }
}
