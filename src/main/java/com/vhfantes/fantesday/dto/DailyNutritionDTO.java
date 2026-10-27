package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Consumo do dia (soma das refeicoes) comparado com a meta.
// caloriesPercent = consumido / meta x 100 (null se a meta de calorias nao foi definida).
public record DailyNutritionDTO(
        LocalDate date,
        MealNutritionDTO consumed,
        NutritionGoalResponseDTO goal,
        NutritionRemainingDTO remaining,
        Integer caloriesPercent) {
}
