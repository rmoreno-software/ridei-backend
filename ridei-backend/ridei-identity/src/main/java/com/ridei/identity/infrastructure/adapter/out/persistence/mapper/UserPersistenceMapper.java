package com.ridei.identity.infrastructure.adapter.out.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ridei.identity.domain.model.User;
import com.ridei.identity.infrastructure.adapter.out.persistence.entity.UserEntity;

/**
 * Infrastructure Mapper responsible for converting between Domain Aggregates and Persistence Entities.
 * <p>
 * <b>Technical Note:</b> We use an abstract class instead of an interface to handle
 * complex mapping logic (reconstitution) without triggering Null-Safety warnings
 * common in strict compilation environments.
 * </p>
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class UserPersistenceMapper {

    /**
     * Converts a Domain Aggregate into a JPA Entity.
     * MapStruct will implement this method automatically.
     */
    public abstract UserEntity toEntity(User user);

    /**
     * Reconstitutes a Domain Aggregate from a JPA Entity.
     * <p>
     * Logic implemented manually to support {@link User#restore}.
     * </p>
     *
     * @param entity The database entity.
     * @return The domain object, or null if entity is null.
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Al ser una clase abstracta normal, el compilador de Java
        // gestiona el flujo de nulos de forma estándar sin conflictos de inferencia.
        return User.restore(
            entity.getId(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getName()
        );
    }
}