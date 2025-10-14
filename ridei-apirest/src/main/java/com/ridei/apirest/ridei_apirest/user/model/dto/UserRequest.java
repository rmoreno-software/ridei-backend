package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.validation.annotation.UniqueEmail;
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

    @NotBlank(message = "Email is required")
    @Email(message = "Email format not valid")
    @UniqueEmail
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Lastame must have between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "LastName is required")
    @Size(min = 2, max = 50, message = "LastName must have between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

}
