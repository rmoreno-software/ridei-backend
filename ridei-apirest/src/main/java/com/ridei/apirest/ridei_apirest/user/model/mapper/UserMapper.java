package com.ridei.apirest.ridei_apirest.user.model.mapper;

import com.ridei.apirest.ridei_apirest.user.model.AuthenticationProvider;
import com.ridei.apirest.ridei_apirest.user.model.Role;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toDto(UserApp user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .pictureUrl(user.getPictureUrl())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserApp toEntity(UserRequest request) {
        if (request == null) return null;

        return UserApp.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword())
                .provider(AuthenticationProvider.LOCAL)
                .role(Role.USER)
                .enabled(true)
                .emailVerified(false)
                .build();
    }
}
