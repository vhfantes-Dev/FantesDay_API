package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.Exercise;

public record ExerciseResponseDTO(
        Long id,
        String name,
        String muscleGroup) {

    public static ExerciseResponseDTO from(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup());
    }
}
