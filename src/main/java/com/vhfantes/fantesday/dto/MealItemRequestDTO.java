package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

public record MealItemRequestDTO(
        Long foodId,
        BigDecimal quantity,
        String unit) {
}
