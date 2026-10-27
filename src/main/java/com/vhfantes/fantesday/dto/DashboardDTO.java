package com.vhfantes.fantesday.dto;

import java.time.LocalDate;
import java.util.List;

// Tudo o que a tela inicial precisa, em uma chamada. strengthRank e null sem peso/sexo ou sem exercicio com rank.
public record DashboardDTO(
        LocalDate date,
        UserResponseDTO user,
        ProgressSummary progress,
        StreakDTO streak,
        TasksSummary tasks,
        DailyNutritionDTO nutrition,
        WaterDayDTO water,
        ActivitiesSummary activities,
        RankDTO strengthRank) {

    // xpToNextLevel = quanto falta para o proximo nivel; levelProgressPercent = 0 a 100 dentro do nivel atual
    public record ProgressSummary(
            int totalXp,
            int level,
            int xpToNextLevel,
            int levelProgressPercent,
            String category) {
    }

    public record TasksSummary(
            int total,
            int completed,
            List<TaskResponseDTO> items) {
    }

    public record ActivitiesSummary(
            int count,
            int totalMinutes,
            int caloriesBurned) {
    }
}
