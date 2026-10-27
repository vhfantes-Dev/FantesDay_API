package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Sequencia de treinos: dias de descanso contam, mas 3 dias seguidos sem treinar quebram o streak.
// expiresOn e o ultimo dia para treinar sem perder o streak atual.
public record StreakDTO(
        int currentStreakDays,
        int longestStreakDays,
        int workoutsInCurrentStreak,
        boolean active,
        LocalDate lastWorkoutDate,
        LocalDate expiresOn) {
}
