package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

import com.vhfantes.fantesday.entity.CatalogSource;
import com.vhfantes.fantesday.entity.Food;

// Valores por 100 g. liquid = true: o app pode registrar em ml (densityGPerMl converte para gramas).
// servingGrams/servingDescription: porcao tipica (ex: 211 g, "1 sanduiche"), para o app sugerir a quantidade.
// createdById: quem cadastrou (so em alimentos CUSTOM); so essa pessoa pode alterar/excluir.
public record FoodResponseDTO(
        Long id,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbsPer100g,
        BigDecimal fatPer100g,
        BigDecimal fiberPer100g,
        String defaultUnit,
        boolean liquid,
        BigDecimal densityGPerMl,
        BigDecimal servingGrams,
        String servingDescription,
        CatalogSource source,
        String externalId,
        Long createdById) {

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
                Boolean.TRUE.equals(food.getLiquid()),
                food.getDensityGPerMl(),
                food.getServingGrams(),
                food.getServingDescription(),
                food.getSource(),
                food.getExternalId(),
                food.getCreatedBy() != null ? food.getCreatedBy().getId() : null);
    }
}
