package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    @NotBlank
    @Size(min = 8, max = 72)
    String newPassword;
}
