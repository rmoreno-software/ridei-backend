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
    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.required}")
    @UniqueEmail
    private String email;

    @Schema(description = "User name", example = "Juan")
    @NotBlank(message = "{user.firstname.required}")
    @Size(min = 2, max = 50, message = "{user.firstname.size}")
    private String firstName;

    @Schema(description = "User lastname", example = "López")
    @NotBlank(message = "{user.lastname.required}")
    @Size(min = 2, max = 50, message = "{user.lastname.size}")
    private String lastName;

    @Schema(description = "User password.", example = "123456")
    @NotBlank(message = "{user.password.required}")
    @Size(min = 6, message = "{user.password.size}")
    private String password;

}
