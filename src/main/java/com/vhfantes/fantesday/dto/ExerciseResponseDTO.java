package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;

public record ExerciseResponseDTO(
        Long id,
        String name,
        String muscleGroup,
        String secondaryMuscles,
        String equipment,
        String level,
        String category,
        String instructions,
        String imageUrl,
        CatalogSource source) {

    public static ExerciseResponseDTO from(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getSecondaryMuscles(),
                exercise.getEquipment(),
                exercise.getLevel(),
                exercise.getCategory(),
                exercise.getInstructions(),
                exercise.getImageUrl(),
                exercise.getSource());
    }
}
