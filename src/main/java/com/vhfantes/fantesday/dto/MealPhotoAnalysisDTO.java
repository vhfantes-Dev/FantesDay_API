package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.util.List;

// Estimativa da IA a partir da foto de uma refeicao (nada e salvo).
// foodId/foodName = alimento do catalogo mais parecido (pode ser null): para registrar, use
// POST /api/meals/{mealId}/items com { "foodId": foodId, "quantity": portionGrams, "unit": "g" }
public record MealPhotoAnalysisDTO(
        String description,
        List<Item> items,
        MealNutritionDTO totals,
        String notes) {

    // confidence = ALTA, MEDIA ou BAIXA
    public record Item(
            String name,
            Integer portionGrams,
            Integer calories,
            BigDecimal protein,
            BigDecimal carbs,
            BigDecimal fat,
            String confidence,
            Long foodId,
            String foodName) {
    }
}
