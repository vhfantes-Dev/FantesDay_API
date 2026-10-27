package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

public record WorkoutRequestDTO(
        String workoutType,
        LocalDate date,
        Integer xpEarned) {
}
