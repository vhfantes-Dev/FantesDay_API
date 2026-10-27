package com.vhfantes.fantesday.dto;

import com.vhfantes.fantesday.entity.Exercise;
import com.vhfantes.fantesday.entity.RoutineExercise;

public record RoutineExerciseResponseDTO(
        Long id,
        Long exerciseId,
        String exerciseName,
        String muscleGroup,
        String imageUrl,
        Integer position,
        Integer targetSets,
        String targetReps,
        Integer restSeconds) {

    public static RoutineExerciseResponseDTO from(RoutineExercise item) {
        Exercise exercise = item.getExercise();
        return new RoutineExerciseResponseDTO(item.getId(), exercise.getId(), exercise.getName(),
                exercise.getMuscleGroup(), exercise.getImageUrl(), item.getPosition(), item.getTargetSets(),
                item.getTargetReps(), item.getRestSeconds());
    }
}
