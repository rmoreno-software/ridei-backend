package com.ridei.apirest.ridei_apirest.user.repository;

import com.ridei.apirest.ridei_apirest.user.model.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserApp, Long> {
    boolean existsByEmail(String email);
}
