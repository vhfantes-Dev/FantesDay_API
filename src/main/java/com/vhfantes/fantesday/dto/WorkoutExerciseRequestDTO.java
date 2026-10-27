package com.vhfantes.fantesday.dto;

// As series sao registradas depois, em /api/workout-exercises/{id}/sets
public record WorkoutExerciseRequestDTO(
        Long exerciseId) {
}
