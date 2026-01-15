package com.ridei.identity.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.out.UserRepository;
import com.ridei.identity.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.ridei.identity.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

@Component
public class PostgresUserRepository implements UserRepository{
    
    private final SpringDataUserRepository springRepository;

    public PostgresUserRepository(SpringDataUserRepository springDataUserRepository) {
        this.springRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepository.findByEmail(email)
            .map(entity -> User.create(
                entity.getEmail(),
                entity.getPassword(),
                entity.getName()
            ));

        // NOTA: Para ser puristas, User.create podría fallar si la password hasheada no cumple reglas.
        // Lo ideal es tener un constructor en User solo para "rehidratar" objetos desde DB sin validaciones.
        // Pero por ahora usaremos create.
    }

    @Override
    public void save(User user) {
        UserEntity entity = new UserEntity(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getName()
        );
        springRepository.save(entity);        
    }

}
