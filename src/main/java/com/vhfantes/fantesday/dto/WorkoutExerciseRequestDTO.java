package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

public record WorkoutExerciseRequestDTO(
        Long exerciseId,
        Integer sets,
        Integer reps,
        BigDecimal weightKg) {
}
