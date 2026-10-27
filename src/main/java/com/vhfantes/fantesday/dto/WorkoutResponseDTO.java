package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

import com.vhfantes.fantesday.entity.Workout;

public record WorkoutResponseDTO(
        Long id,
        Long userId,
        String workoutType,
        LocalDate date,
        Integer xpEarned) {

    public static WorkoutResponseDTO from(Workout workout) {
        return new WorkoutResponseDTO(
                workout.getId(),
                workout.getUser().getId(),
                workout.getWorkoutType(),
                workout.getDate(),
                workout.getXpEarned());
    }
}
