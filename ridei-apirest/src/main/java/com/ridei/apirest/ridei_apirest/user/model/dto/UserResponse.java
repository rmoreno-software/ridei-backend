package com.ridei.apirest.ridei_apirest.user.model.dto;

import com.ridei.apirest.ridei_apirest.user.model.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}
