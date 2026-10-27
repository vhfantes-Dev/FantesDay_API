package com.vhfantes.fantesday.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vhfantes.fantesday.entity.WorkoutExercise;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

    List<WorkoutExercise> findByWorkoutId(Long workoutId);

    boolean existsByExerciseId(Long exerciseId);

    @Modifying
    @Query("delete from WorkoutExercise we where we.workout.id in (select w.id from Workout w where w.user.id = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
