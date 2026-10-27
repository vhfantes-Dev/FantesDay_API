package com.vhfantes.fantesday.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Sem data, registra o peso de hoje. Registrar de novo no mesmo dia substitui o valor.
public record WeightRequestDTO(
        BigDecimal weightKg,
        LocalDate date) {
}
