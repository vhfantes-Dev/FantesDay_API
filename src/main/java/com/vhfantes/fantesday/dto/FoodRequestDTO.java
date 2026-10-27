package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Valores nutricionais sempre por 100 g. Para liquidos, liquid = true permite registrar em ml
// (densityGPerMl converte ml em gramas; sem densidade, vale 1 g/ml).
public record FoodRequestDTO(
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatPer100g,
        BigDecimal fiberPer100g,
        String defaultUnit,
        Boolean liquid,
        BigDecimal densityGPerMl) {
}
