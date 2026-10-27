package com.vhfantes.fantesday.dto;

public record ExerciseRequestDTO(
        String name,
        String muscleGroup,
        String secondaryMuscles,
        String equipment,
        String level,
        String category,
        String instructions,
        String imageUrl) {
}
