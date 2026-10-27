package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.vhfantes.fantesday.entity.NutritionGoal;

// Campos null = meta ainda nao definida
public record NutritionGoalResponseDTO(
        Long userId,
        Integer dailyCalories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG,
        LocalDateTime updatedAt) {

    public static NutritionGoalResponseDTO from(NutritionGoal goal) {
        return new NutritionGoalResponseDTO(
                goal.getUserId(),
                goal.getDailyCalories(),
                goal.getProteinG(),
                goal.getCarbsG(),
                goal.getFatG(),
                goal.getUpdatedAt());
    }

    public static NutritionGoalResponseDTO empty(Long userId) {
        return new NutritionGoalResponseDTO(userId, null, null, null, null, null);
    }
}
