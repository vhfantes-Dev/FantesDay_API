package com.vhfantes.fantesday.dto;

import java.time.LocalDate;

// Sem nome, a refeicao recebe "Refeição N" (N = ordem no dia)
public record MealRequestDTO(
        String name,
        LocalDate date) {
}
