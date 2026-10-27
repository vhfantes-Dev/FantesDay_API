package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Ranking semanal por volume levantado (soma de reps x carga) na semana atual
public record WeeklyLeaderboardEntryDTO(
        int position,
        Long userId,
        String name,
        BigDecimal volumeKg,
        long workouts) {
}
