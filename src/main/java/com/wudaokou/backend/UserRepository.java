package com.wudaokou.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    Boolean existsByToken(String token);
    Optional<User> findByToken(String token);
}
