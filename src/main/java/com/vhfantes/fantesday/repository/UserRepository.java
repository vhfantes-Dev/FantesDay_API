package com.vhfantes.fantesday.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
