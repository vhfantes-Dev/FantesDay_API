package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.Food;

public record FoodResponseDTO(
        Long id,
        String name,
        BigDecimal caloriesPer100g,
        String defaultUnit) {

    public static FoodResponseDTO from(Food food) {
        return new FoodResponseDTO(
                food.getId(),
                food.getName(),
                food.getCaloriesPer100g(),
                food.getDefaultUnit());
    }
}
