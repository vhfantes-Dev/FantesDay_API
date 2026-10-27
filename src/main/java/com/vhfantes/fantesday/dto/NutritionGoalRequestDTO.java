package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Metas diarias; so os campos enviados sao alterados
public record NutritionGoalRequestDTO(
        Integer dailyCalories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG,
        Integer dailyWaterMl) {
}
