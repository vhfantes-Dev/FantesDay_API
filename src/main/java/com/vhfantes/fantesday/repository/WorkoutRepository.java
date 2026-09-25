package com.vhfantes.fantesday.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.Workout;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    List<Workout> findByUserId(Long userId);
}
