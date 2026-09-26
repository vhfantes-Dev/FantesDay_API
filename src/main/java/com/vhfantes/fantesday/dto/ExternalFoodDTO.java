package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Produto encontrado no Open Food Facts que ainda nao foi salvo no banco.
// Para usar numa refeicao, importe pelo codigo de barras.
public record ExternalFoodDTO(
        String barcode,
        String name,
        String brand,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatPer100g,
        BigDecimal fiberPer100g) {
}
