package com.ridei.identity.infrastructure.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ridei.identity.application.port.in.RegisterUserCommand;
import com.ridei.identity.infrastructure.adapter.in.web.dto.RegisterRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthMapper {

    // MapStruct mapea automáticamente los campos con el mismo nombre
    // (email -> email, password -> password, name -> name)
    RegisterUserCommand toCommand(RegisterRequest request);
    
}
