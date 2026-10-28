package com.vhfantes.fantesday.seed;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Espelha um item de resources/seed/extra-foods.json (valores por 100 g, ou 100 ml nos liquidos).
// O arquivo tambem traz sourceUrl e note (de onde vieram os numeros), que nao vao para o banco.
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtraFoodSeed(
        String externalId,
        String name,
        String category,
        BigDecimal servingGrams,
        String servingDescription,
        BigDecimal energyKcal,
        BigDecimal protein,
        BigDecimal carbohydrate,
        BigDecimal lipid,
        BigDecimal fiber,
        Boolean liquid,
        BigDecimal densityGPerMl) {
}
