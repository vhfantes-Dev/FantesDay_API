package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.MealItem;

public record MealItemResponseDTO(
        Long id,
        Long mealId,
        Long foodId,
        String foodName,
        BigDecimal quantity,
        String unit,
        Integer calculatedCalories,
        BigDecimal calculatedProtein,
        BigDecimal calculatedCarbs,
        BigDecimal calculatedFat) {

    public static MealItemResponseDTO from(MealItem item) {
        return new MealItemResponseDTO(
                item.getId(),
                item.getMeal().getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getCalculatedCalories(),
                item.getCalculatedProtein(),
                item.getCalculatedCarbs(),
                item.getCalculatedFat());
    }
}
