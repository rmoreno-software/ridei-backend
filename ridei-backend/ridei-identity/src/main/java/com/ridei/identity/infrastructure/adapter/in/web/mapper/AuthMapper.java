package com.ridei.identity.infrastructure.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ridei.identity.application.port.in.RegisterUserCommand;
import com.ridei.identity.infrastructure.adapter.in.web.dto.RegisterRequest;

/**
 * Infrastructure Mapper responsible for transforming Data Transfer Objects (DTOs) into Domain Commands.
 * <p>
 * This interface utilizes <b>MapStruct</b> to generate the implementation code at compile-time.
 * It serves as a translation layer that decouples the external API contract ({@link RegisterRequest})
 * from the internal business intent ({@link RegisterUserCommand}).
 * </p>
 * <p>
 * <b>Spring Integration:</b> annotated with {@code componentModel = "spring"}, allowing the
 * generated implementation to be automatically detected and injected as a Spring Bean.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    /**
     * Converts a web registration request into a domain-agnostic command.
     * <p>
     * <b>Mapping Strategy:</b>
     * <ul>
     * <li>Fields are mapped by name convention (e.g., {@code request.email} -> {@code command.email}).</li>
     * <li>Type conversions are handled automatically by MapStruct.</li>
     * </ul>
     * </p>
     *
     * @param request The incoming HTTP request payload validated by the Controller.
     * @return An immutable {@link RegisterUserCommand} ready for the Use Case.
     */
    RegisterUserCommand toCommand(RegisterRequest request);
    
}
