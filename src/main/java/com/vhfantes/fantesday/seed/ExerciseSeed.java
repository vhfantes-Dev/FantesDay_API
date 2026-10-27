package com.vhfantes.fantesday.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vhfantes.fantesday.entity.CatalogSource;

// Espelha um item de resources/seed/exercises-pt.json: free-exercise-db traduzido + wger em portugues.
// descriptionLanguage: "pt" ou "en" (alguns exercicios do free-exercise-db ainda tem instrucoes em ingles).
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExerciseSeed(
        CatalogSource source,
        String externalId,
        String name,
        String nameEn,
        String description,
        String descriptionLanguage,
        String muscleGroup,
        String secondaryMuscles,
        String equipment,
        String level,
        String category,
        String imageUrl) {
}
