package com.vhfantes.fantesday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
