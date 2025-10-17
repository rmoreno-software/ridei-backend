package com.ridei.apirest.ridei_apirest.user.model.mapper;

import com.ridei.apirest.ridei_apirest.user.model.AuthenticationProvider;
import com.ridei.apirest.ridei_apirest.user.model.Role;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserRequest;
import com.ridei.apirest.ridei_apirest.user.model.dto.UserResponse;
import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import org.springframework.stereotype.Component;

/**
 * Mapper class responsible for converting between {@link UserApp} entities
 * and their corresponding Data Transfer Objects (DTOs) such as
 * {@link UserRequest} and {@link UserResponse}.
 *
 * <p>This component isolates conversion logic between layers, ensuring
 * clean separation of concerns and preventing tight coupling between
 * the persistence and presentation layers.</p>
 *
 * <p><b>Conversion directions:</b></p>
 * <ul>
 *     <li><b>Entity ➡ DTO:</b> {@link #toDto(UserApp)} converts a {@link UserApp}
 *          entity into a {@link UserResponse} for API responses.</li>
 *     <li><b>DTO ➡ Entity:</b> {@link #toEntity(UserRequest)} converts a
 *          {@link UserRequest} (usually received from a client) into a {@link UserApp}
 *          entity for persistence.</li>
 * </ul>
 *
 * <p><b>Default values applied during mapping:</b></p>
 * <ul>
 *     <li>{@link AuthenticationProvider} is set to {@code LOCAL} by default.</li>
 *     <li>{@link Role} is set to {@code USER} by default.</li>
 *     <li>{@code enabled} is set to {@code true} and {@code emailVerified} to {@code false}
 *          entity for persistence.</li>
 * </ul>
 *
 * <p>Annotated with {@link Component} to be automatically detected
 * and managed by Spring's dependency injection container.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     // Convert an entity to DTO
 *     UserResponse response = userMapper.toDto(userEntity);
 *
 *     // Convert a request to entity
 *     UserApp entity = userMapper.toEntity(userRequest);
 * </pre>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Component
public class UserMapper {

    /**
     * Converts a {@link UserApp} entity into a {@link UserResponse} DTO.
     *
     * <p>This method extracts only relevant public-facing data,
     * omitting sensitive fields such as passwords or internal states.</p>
     *
     * @param user the {@link UserApp} entity to convert; may be {@code null}
     * @return a {@link UserResponse} containing mapped data, or {@code null} if the inputs is {@code null}
     */
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

    /**
     * Converts a {@link UserRequest} DTO into a {@link UserApp} entity.
     *
     * <p>This method is typically used when creating a new user record
     * from incoming API request data.</p>
     *
     * <p>By default, the following fields are initialized:</p>
     * <ul>
     *     <li>{@code provider} ➡ {@link AuthenticationProvider#LOCAL}</li>
     *     <li>{@code role} ➡ {@link Role#USER}</li>
     *     <li>{@code enabled} ➡ {@code true}</li>
     *     <li>{@code emailVerified} ➡ {@code false}</li>
     * </ul>
     *
     * @param request the {@link UserRequest} containing incoming user data; may be {@code null}
     * @return a new {@link UserApp} entity ready for persistence, or {@code null} if the input is {@code null}
     */
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
