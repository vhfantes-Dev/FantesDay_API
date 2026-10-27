package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;

public record FoodResponseDTO(
        Long id,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatPer100g,
        BigDecimal fiberPer100g,
        String defaultUnit,
        CatalogSource source,
        String externalId) {

    public static FoodResponseDTO from(Food food) {
        return new FoodResponseDTO(
                food.getId(),
                food.getName(),
                food.getCaloriesPer100g(),
                food.getProteinPer100g(),
                food.getCarbsPer100g(),
                food.getFatPer100g(),
                food.getFiberPer100g(),
                food.getDefaultUnit(),
                food.getSource(),
                food.getExternalId());
    }
}
