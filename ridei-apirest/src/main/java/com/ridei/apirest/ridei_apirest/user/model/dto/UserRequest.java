package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.validation.annotation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @Schema(description = "User email", example = "usuario@dominio.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email format not valid")
    @UniqueEmail
    private String email;

    @Schema(description = "User name", example = "Juan")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Lastame must have between 2 and 50 characters")
    private String firstName;

    @Schema(description = "User lastname", example = "López")
    @NotBlank(message = "LastName is required")
    @Size(min = 2, max = 50, message = "LastName must have between 2 and 50 characters")
    private String lastName;

    @Schema(description = "User password.", example = "123456")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

}
