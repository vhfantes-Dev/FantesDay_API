package com.vhfantes.fantesday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.Progress;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    
}
