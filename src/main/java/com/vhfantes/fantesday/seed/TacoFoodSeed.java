package com.vhfantes.fantesday.seed;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Espelha um item de resources/seed/taco.json (valores por 100g)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TacoFoodSeed(
        Integer id,
        String description,
        String category,
        BigDecimal energyKcal,
        BigDecimal protein,
        BigDecimal carbohydrate,
        BigDecimal lipid,
        BigDecimal fiber,
        Boolean liquid,
        BigDecimal densityGPerMl) {
}
