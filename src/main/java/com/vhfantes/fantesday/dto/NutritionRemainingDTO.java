package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Quanto falta para a meta (negativo se passou). Cada campo e null se aquela meta nao foi definida.
public record NutritionRemainingDTO(
        Integer calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat) {
}
