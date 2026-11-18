package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.util.List;

// Estimativa da IA a partir da foto de uma refeicao. A refeicao nao e salva aqui.
// foodId/foodName = alimento do catalogo mais parecido. Quando o catalogo nao tem o item, a API cadastra um
// alimento do usuario com os valores estimados (estimatedFood = true, tambem quando ele veio de uma foto anterior). Para registrar, use
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
            String foodName,
            boolean estimatedFood) {
    }
}
