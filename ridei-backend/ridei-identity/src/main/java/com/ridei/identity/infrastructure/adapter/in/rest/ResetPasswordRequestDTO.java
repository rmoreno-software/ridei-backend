package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.temporary_password.required}")
    private String temporaryPassword;

    @NotBlank(message = "{validation.new_password.required}")
    @Size(min = 8, max = 72, message = "{validation.password.size_range}")
    private String newPassword;
    
}
