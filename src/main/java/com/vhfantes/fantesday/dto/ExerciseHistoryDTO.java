package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Melhor 1RM estimado e volume (reps x carga) de um exercicio em um dia de treino
public record ExerciseHistoryDTO(
        LocalDate date,
        BigDecimal bestOneRepMax,
        BigDecimal volumeKg) {
}
