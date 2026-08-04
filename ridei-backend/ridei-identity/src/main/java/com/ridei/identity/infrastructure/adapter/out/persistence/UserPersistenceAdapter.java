package com.ridei.identity.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.model.Username;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    @Override
    public void save(User user) {
        jpaRepository.save(UserJpaEntity.fromDomain(user));
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByUsername(Username username) {
        return jpaRepository.existsByUsername(username.value());
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return jpaRepository.findByGoogleId(googleId).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<UserProfile> findByIdWithProfile(UserId id) {
        return jpaRepository.findById(id.value())
            .map(entity -> new UserProfile(
                new UserId(entity.getId()),
                new Email(entity.getEmail()),
                entity.getUsername() != null ? new Username(entity.getUsername()) : null,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getGender(),
                entity.getRole(),
                entity.getAccountStatus(),
                entity.getRiderProfile() != null ? entity.getRiderProfile().getProfileType() : null,
                entity.getRiderProfile() != null ? entity.getRiderProfile().getRacingLicenseNumber() : null,
                entity.getDateOfBirth(),
                entity.getCountryCode(),
                entity.getPhoneNumber() != null ? new PhoneNumber(entity.getPhoneNumber()) : null,
                entity.getPictureUrl()
            ));
    }

    @Override
    public void update(User user) {
        jpaRepository.save(UserJpaEntity.fromDomain(user));
    }
}
