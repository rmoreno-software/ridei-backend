package com.ridei.identity.domain.port.out;

import java.util.Optional;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.Username;

public interface UserRepositoryPort {
    void save(User user);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
    Optional<User> findByEmail(Email email);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findById(UserId id);
}
