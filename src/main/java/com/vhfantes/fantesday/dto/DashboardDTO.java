package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
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
        CaloriesBalance calories,
        WaterDayDTO water,
        ActivitiesSummary activities,
        WorkoutsSummary workouts,
        BodyMetricsDTO bodyMetrics,
        WeightSummary weight,
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

    // Treinos do dia e quantos foram feitos na semana (segunda a domingo)
    public record WorkoutsSummary(
            List<WorkoutResponseDTO> today,
            int thisWeek) {
    }

    // target = meta de calorias (ou o gasto diario, se nao houver meta);
    // remaining = target + gasto nas atividades - consumido (negativo = passou)
    public record CaloriesBalance(
            Integer target,
            String targetSource,
            int consumed,
            int burnedByActivities,
            Integer remaining) {
    }

    // changeLast30Days = peso atual - peso de 30 dias atras (null sem registros suficientes)
    public record WeightSummary(
            BigDecimal currentKg,
            BigDecimal changeLast30Days,
            LocalDate lastEntryDate) {
    }
}
