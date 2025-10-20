package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.validation.annotation.MailMustExists;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @MailMustExists
    private String email;

    @NotBlank
    private String password;
}
