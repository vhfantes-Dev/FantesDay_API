package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;

// createdById: quem cadastrou (so em exercicios CUSTOM); so essa pessoa pode alterar/excluir
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
        CatalogSource source,
        Long createdById) {

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
                exercise.getSource(),
                exercise.getCreatedBy() != null ? exercise.getCreatedBy().getId() : null);
    }
}
