package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.Username;

public interface UserRepositoryPort {
    void save(User user);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
}
