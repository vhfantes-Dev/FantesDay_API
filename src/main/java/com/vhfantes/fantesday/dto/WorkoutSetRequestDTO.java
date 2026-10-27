package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;

// Em exercicios com peso corporal (barra fixa, paralelas), weightKg e a carga extra (0 ou null = sem carga)
public record WorkoutSetRequestDTO(
        Integer reps,
        BigDecimal weightKg) {
}
