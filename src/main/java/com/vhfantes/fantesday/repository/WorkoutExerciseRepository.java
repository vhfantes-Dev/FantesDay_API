package com.vhfantes.fantesday.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.WorkoutExercise;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    List<WorkoutExercise> findByWorkoutId(Long workoutId);
}
