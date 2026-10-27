package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.WorkoutSet;

// rank: rank atual do usuario no exercicio depois da serie (null se o exercicio nao tem rank
// ou se o usuario nao informou peso corporal e sexo)
public record WorkoutSetResponseDTO(
        Long id,
        Long workoutExerciseId,
        Long exerciseId,
        String exerciseName,
        Integer reps,
        BigDecimal weightKg,
        BigDecimal estimatedOneRepMax,
        Boolean personalRecord,
        LocalDateTime createdAt,
        ExerciseRankDTO rank) {

    public static WorkoutSetResponseDTO from(WorkoutSet set, ExerciseRankDTO rank) {
        return new WorkoutSetResponseDTO(
                set.getId(),
                set.getWorkoutExercise().getId(),
                set.getWorkoutExercise().getExercise().getId(),
                set.getWorkoutExercise().getExercise().getName(),
                set.getReps(),
                set.getWeightKg(),
                set.getEstimatedOneRepMax(),
                set.getPersonalRecord(),
                set.getCreatedAt(),
                rank);
    }

    public static WorkoutSetResponseDTO from(WorkoutSet set) {
        return from(set, null);
    }
}
