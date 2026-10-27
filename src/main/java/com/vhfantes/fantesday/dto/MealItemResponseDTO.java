package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.MealItem;
import com.vhfantes.fantesday.food.FoodUnits;

// unit: "g" ou "ml"; quantityInGrams e a quantidade usada no calculo (ml x densidade do alimento)
public record MealItemResponseDTO(
        Long id,
        Long mealId,
        Long foodId,
        String foodName,
        BigDecimal quantity,
        String unit,
        BigDecimal quantityInGrams,
        Integer calculatedCalories,
        BigDecimal calculatedProtein,
        BigDecimal calculatedCarbs,
        BigDecimal calculatedFat) {

    public static MealItemResponseDTO from(MealItem item) {
        String unit = FoodUnits.normalizeLenient(item.getUnit(), item.getFood());
        return new MealItemResponseDTO(
                item.getId(),
                item.getMeal().getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getQuantity(),
                unit,
                FoodUnits.toGrams(item.getQuantity(), unit, item.getFood()),
                item.getCalculatedCalories(),
                item.getCalculatedProtein(),
                item.getCalculatedCarbs(),
                item.getCalculatedFat());
    }
}
