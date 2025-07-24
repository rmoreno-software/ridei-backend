package com.ridei.backend.ridei_api_rest.repository;

import com.ridei.backend.ridei_api_rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
