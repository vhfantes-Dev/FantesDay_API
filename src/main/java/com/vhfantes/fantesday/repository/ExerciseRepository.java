package com.vhfantes.fantesday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

}
