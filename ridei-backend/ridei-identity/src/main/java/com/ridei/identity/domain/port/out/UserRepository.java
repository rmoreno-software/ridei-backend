package com.ridei.identity.domain.port.out;

import java.util.Optional;

import com.ridei.identity.domain.model.User;

public interface UserRepository {
    
    void save(User user);

    Optional<User> findByEmail(String email);
}
