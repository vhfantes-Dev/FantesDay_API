package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Exercise;

// name em portugues e nameEn com o nome original. instructionsLanguage: "pt" ou "en".
// createdById: quem cadastrou (so em exercicios CUSTOM); so essa pessoa pode alterar/excluir.
public record ExerciseResponseDTO(
        Long id,
        String name,
        String nameEn,
        String muscleGroup,
        String secondaryMuscles,
        String equipment,
        String level,
        String category,
        String instructions,
        String instructionsLanguage,
        String imageUrl,
        CatalogSource source,
        Long createdById) {

    public static ExerciseResponseDTO from(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getNameEn(),
                exercise.getMuscleGroup(),
                exercise.getSecondaryMuscles(),
                exercise.getEquipment(),
                exercise.getLevel(),
                exercise.getCategory(),
                exercise.getInstructions(),
                exercise.getInstructionsLanguage(),
                exercise.getImageUrl(),
                exercise.getSource(),
                exercise.getCreatedBy() != null ? exercise.getCreatedBy().getId() : null);
    }
}
