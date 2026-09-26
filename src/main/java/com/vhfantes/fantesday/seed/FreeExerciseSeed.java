package com.vhfantes.fantesday.seed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Espelha um item de resources/seed/exercises.json (free-exercise-db)
@JsonIgnoreProperties(ignoreUnknown = true)
public record FreeExerciseSeed(
        String id,
        String name,
        String level,
        String equipment,
        String category,
        List<String> primaryMuscles,
        List<String> secondaryMuscles,
        List<String> instructions,
        List<String> images) {
}
