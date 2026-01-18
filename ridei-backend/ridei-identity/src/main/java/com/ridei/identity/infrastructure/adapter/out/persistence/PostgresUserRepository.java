package com.ridei.identity.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.out.UserRepository;
import com.ridei.identity.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.ridei.identity.infrastructure.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.ridei.identity.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Secondary Adapter (Driven Adapter) implementation for User Persistence.
 * <p>
 * This class acts as the bridge between the Domain Layer ({@link UserRepository})
 * and the Infrastructure Layer (Spring Data JPA). Its main responsibility is
 * <b>Mapping</b>:
 * <ul>
 * <li><b>Read:</b> Converts Infrastructure Entities ({@link UserEntity}) -> Domain Aggregates ({@link User}).</li>
 * <li><b>Write:</b> Converts Domain Aggregates ({@link User}) -> Infrastructure Entities ({@link UserEntity}).</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PostgresUserRepository implements UserRepository{
    
    private final SpringDataUserRepository springRepository;
    private final UserPersistenceMapper userMapper;

    /**
     * {@inheritDoc}
     * <p>
     * <b>Mapping Logic (Read):</b>
     * Reconstitutes the Domain Aggregate using the {@code User.restore()} factory method.
     * This ensures we preserve the original UUID from the database instead of generating a new one.
     * </p>
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return springRepository.findByEmail(email)
            .map(userMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Mapping Logic (Write):</b>
     * Extracts the state from the Domain Aggregate to create a JPA Entity
     * compatible with the underlying database schema.
     * </p>
     */
    @Override
    public void save(User user) {
        // 1. Convert Domain -> Entity usando el mapper
        UserEntity entity = userMapper.toEntity(user);
        
        // 2. Persist
        springRepository.save(entity);       
    }

}
