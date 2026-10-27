package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.WorkoutExercise;

public record WorkoutExerciseResponseDTO(
        Long id,
        Long workoutId,
        Long exerciseId,
        String exerciseName) {

    public static WorkoutExerciseResponseDTO from(WorkoutExercise item) {
        return new WorkoutExerciseResponseDTO(
                item.getId(),
                item.getWorkout().getId(),
                item.getExercise().getId(),
                item.getExercise().getName());
    }
}
