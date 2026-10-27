package com.vhfantes.fantesday.dto;

import java.util.List;

// Treino criado a partir de uma rotina: registre as series em /api/workout-exercises/{workoutExerciseId}/sets
public record StartedWorkoutDTO(
        WorkoutResponseDTO workout,
        List<PlannedExercise> exercises) {

    public record PlannedExercise(
            Long workoutExerciseId,
            Long exerciseId,
            String exerciseName,
            Integer targetSets,
            String targetReps,
            Integer restSeconds) {
    }
}
