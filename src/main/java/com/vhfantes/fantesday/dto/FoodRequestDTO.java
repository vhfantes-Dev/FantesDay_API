package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

public record FoodRequestDTO(
        String name,
        BigDecimal caloriesPer100g,
        String defaultUnit) {
}
