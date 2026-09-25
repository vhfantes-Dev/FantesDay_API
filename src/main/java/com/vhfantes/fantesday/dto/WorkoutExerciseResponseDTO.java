package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.WorkoutExercise;

public record WorkoutExerciseResponseDTO(
        Long id,
        Long workoutId,
        Long exerciseId,
        String exerciseName,
        Integer sets,
        Integer reps,
        BigDecimal weightKg) {

    public static WorkoutExerciseResponseDTO from(WorkoutExercise item) {
        return new WorkoutExerciseResponseDTO(
                item.getId(),
                item.getWorkout().getId(),
                item.getExercise().getId(),
                item.getExercise().getName(),
                item.getSets(),
                item.getReps(),
                item.getWeightKg());
    }
}
