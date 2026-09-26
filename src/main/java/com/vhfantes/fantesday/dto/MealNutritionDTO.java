package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

public record MealNutritionDTO(
        int calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat) {
}
